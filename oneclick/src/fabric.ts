import path from "path";
import { downloadFile, mkdir, uaheaderfull, writeConfig, writeEula, writeRun } from "./utils.js";
import { downloadMod } from "./modrinth.js";

interface BaseVersion {
    version: string
    stable: boolean
}

interface MavenVersion extends BaseVersion {
    maven: string
}

interface LoaderVersion extends MavenVersion {
    separator: string
    build: number
}

interface InstallerVersion extends MavenVersion {
    url: string
}

type AllVersions = {
    game: BaseVersion[]
    //mappings
    //intermediary
    loader: LoaderVersion[]
    installer: InstallerVersion[]
}

export function findLatest<Version extends { stable: boolean }>(versions: Version[]): Version {
    return versions.find(version => version.stable) ?? versions[0]
}

export async function downloadFabric(to: string) {
    const root = await mkdir(path.resolve(to, "Fabric"), { empty: true })
    await writeEula(root)

    const configs = await mkdir(path.resolve(root, "config"))
    const config = path.resolve(configs, "crowdcontrol.conf")
    await writeConfig(config)

    console.info(`Fetching Fabric versions`)
    const versions = await fetch(`https://meta.fabricmc.net/v2/versions`, uaheaderfull).then(r => r.json() as Promise<AllVersions>)
    const latestGame = versions.game.find(ver => ver.version === '1.21.4')! // TODO findLatest(versions.game)
    const latestLoader = findLatest(versions.loader)
    const latestInstaller = findLatest(versions.installer)
    
    console.info(`Downloading Fabric ${latestGame.version}`)
    const serverJar = path.resolve(root, "fabric.jar")
    const serverUrl = `https://meta.fabricmc.net/v2/versions/loader/${latestGame.version}/${latestLoader.version}/${latestInstaller.version}/server/jar`
    await downloadFile(serverJar, serverUrl)

    await writeRun(serverJar, 21)

    const mods = await mkdir(path.resolve(root, "mods"))
    await downloadMod(mods, "fabric", latestGame.version, { project: "fabric-api", filename: "fabric-api.jar" })
    await downloadMod(mods, "fabric", latestGame.version, { project: "lithium", filename: "Lithium.jar" })
    return await downloadMod(mods, "fabric", latestGame.version, { filename: "CrowdControl-Fabric.jar" })
}