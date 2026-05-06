import path from "path";
import { downloadFile, jrePaths, mkdir, uaheaderfull, writeConfig, writeEula, writeRun } from "./utils.js";
import { downloadMod } from "./modrinth.js";

interface BaseBody {
    project: {
        id: string
        name: string
    }
}

interface ProjectBody extends BaseBody {
    versions: Record<string, string[]>
}

interface Build {
    id: number
    time: string
    channel: 'STABLE' | 'BETA' | 'RECOMMENDED' | 'ALPHA'
    // commits
    downloads: {
        "server:default": {
            name: string
            checksums: {
                sha256: string
            }
            size: number
            url: string
        }
    }
}

type BuildsBody = Build[]

export async function fetchLatest(version: string, force?: boolean) {
    const data = await fetch(`https://fill.papermc.io/v3/projects/paper/versions/${version}/builds`, uaheaderfull).then(r => r.json() as Promise<BuildsBody>)
    return force
        ? data[0]
        : data.find(build => build.channel === "STABLE")
}

export async function downloadPaper(to: string, forceVersion: string, java: keyof (typeof jrePaths)) {
    try {
        const root = await mkdir(path.resolve(to, "Paper"), { empty: true })
        await writeEula(root)

        const plugins = await mkdir(path.resolve(root, "plugins"))
        const configs = await mkdir(path.resolve(plugins, "CrowdControl"))
        const config = path.resolve(configs, "config.yml")
        await writeConfig(config)

        console.info("Fetching Paper versions")
        const data = await fetch(`https://fill.papermc.io/v3/projects/paper`, uaheaderfull).then(r => r.json() as Promise<ProjectBody>)
        let build: Build | undefined
        let version: string | undefined
        for (const vers of Object.values(data.versions).flat().toReversed()) {
            if (forceVersion && vers !== forceVersion) continue;
            build = await fetchLatest(vers, !!forceVersion)
            if (build) {
                version = vers
                break
            }
        }

        if (!build || !version) throw new Error("Could not fetch valid Paper version")
        
        console.info(`Downloading Paper ${version}`)
        const serverJar = path.resolve(root, "paper.jar")
        const paperDlUrl = build.downloads["server:default"].url
        await downloadFile(serverJar, paperDlUrl)

        await writeRun(serverJar, java)
        
        return await downloadMod(plugins, "paper", version, { filename: "CrowdControl-Paper.jar" })
    } catch (e) {
        console.error("Help", e)
    }
}