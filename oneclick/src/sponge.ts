import path from "path";
import fs from "fs-extra";
import { downloadFile, isSemverGE, jrePaths, mkdir, uaheaderfull, writeConfig, writeEula, writeRun } from "./utils.js";
import { downloadMod } from "./modrinth.js";
import child_process from "child_process";

type SpongeError = {
    name: string
    detail: string
}

type Tags = {
    api: string
    forge: string
    minecraft: string
}

type User = {
    name: string
    email: string
}

type ArtifactBody = {
    coordinates: {
        groupId: string
        artifactId: string
        version: string
    }
    commit: {
        commits: {
            commit: {
                message: string
                body: string
                sha: string
                author: User
                commiter: User
                link: string
                commitDate: string
            }
            submoduleCommits: [] // idk
        }[]
        processing: boolean
    }
    assets: {
        classifier: string
        downloadUrl: string
        md5: string
        sha1: string
        extension: "pom" | "jar"
    }[]
    tags: Tags
    recommended: boolean
}

interface Paginated {
    offset: number
    limit: number
    size: number
}

interface VersionsBody extends Paginated {
    artifacts: {
        [key: string]: {
            tagValues: Tags
            recommended: boolean
        }
    }
}

export async function tryGet(url: string) {
    const body = await fetch(url, uaheaderfull).then(r => r.json() as Promise<VersionsBody | SpongeError>)
    if ("name" in body) return undefined
    return body
}

export async function tryGetRecommended(url: string) {
    const recUrl = url + "&recommended=true"
    return await tryGet(recUrl) ?? await tryGet(url)
}

export async function downloadSponge(to: string, api: number) {
    const root = await mkdir(path.resolve(to, `Sponge${api}`), { empty: true })
    await writeEula(root)

    const configs = await mkdir(path.resolve(root, "config"))
    const config = path.resolve(configs, "crowdcontrol.conf")
    await writeConfig(config)

    console.info(`Fetching SpongeForge ${api} versions`)
    const version = await tryGetRecommended(`https://dl-api.spongepowered.org/v2/groups/org.spongepowered/artifacts/spongeforge/versions?limit=1&tags=api:${api}`)
    if (!version) throw new Error(`Unable to find SpongeForge ${api}`);
    
    const versionId = Object.keys(version.artifacts)[0]
    console.info(`Fetching SpongeForge ${versionId} data`)
    const artifact = await fetch(`https://dl-api.spongepowered.org/v2/groups/org.spongepowered/artifacts/spongeforge/versions/${versionId}`, uaheaderfull).then(r => r.json() as Promise<ArtifactBody>)

    const { minecraft, forge } = artifact.tags
    const assets = artifact.assets.filter(asset => asset.extension === "jar")
    const asset = assets.find(a => a.classifier === "universal") ?? assets.find(a => a.classifier === "")
    if (!asset) throw new Error(`Unable to find download for SpongeForge ${versionId}`);
    const spongeUrl = asset.downloadUrl
    
    const mods = await mkdir(path.resolve(root, "mods"))
    const spongeJar = path.resolve(mods, "SpongeForge.jar")
    await downloadFile(spongeJar, spongeUrl)

    const forgeFull = api === 7
        ? `${minecraft}-14.23.5.2859` // update to fix uhh
        : api === 8
            ? `${minecraft}-36.2.34` // update to fix issue with new JREs
            : `${minecraft}-${forge}`
    console.info(`Downloading Forge ${forgeFull} installer`)
    const installerJar = path.resolve(to, `forge-${forgeFull}.jar`)
    const installerUrl = `https://maven.minecraftforge.net/net/minecraftforge/forge/${forgeFull}/forge-${forgeFull}-installer.jar`
    await downloadFile(installerJar, installerUrl)

    console.info(`Running Forge ${forgeFull} installer`)
    const child = child_process.spawn("java", ["-jar", installerJar, "--installServer"], {
        cwd: root,
    })
    for await (const data of child.stdout) {
        // no-op (this is the only way to block until the installer finishes)
    }
    await fs.unlink(installerJar)

    if (api < 11) {
        const serverJar = (await fs.readdir(root)).find(file => file.startsWith("forge") && path.extname(file) === ".jar")
        if (!serverJar) throw new Error(`Could not find server jar for Forge ${forgeFull}`);
        const resolved = path.join(root, serverJar)
        await writeRun(resolved, 8)
    } else {
        const jre = jrePaths[21]
        const batFile = path.resolve(root, "run.bat")
        await fs.writeFile(batFile, `@echo off
start ..\\java\\${jre}\\bin\\java.exe -Xmx2048M -Xms2048M @libraries/net/minecraftforge/forge/${forgeFull}/win_args.txt nogui %* > log.txt 2> errorlog.txt`)
    }

    const plugins = await mkdir(path.resolve(mods, "plugins"))
    return await downloadMod(plugins, "sponge", minecraft)
}