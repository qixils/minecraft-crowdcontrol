import path from "path";
import { downloadFile, mkdir, uaheaderfull, writeConfig, writeEula, writeRun } from "./utils.js";
import { downloadMod } from "./modrinth.js";

interface BaseBody {
    project_id: string
    project_name: string
}

interface ProjectBody extends BaseBody {
    version_groups: string[]
    versions: string[]
}

interface BuildsBody extends BaseBody {
    version: string
    builds: Build[]
}

interface Build {
    build: number,
    time: string,
    channel: "default" | "experimental",
    promoted: boolean,
    changes: {
        commit: string,
        summary: string,
        message: string,
    }[],
    downloads: {
        application: {
            name: string,
            sha256: string,
        }
    }
}

export async function fetchLatest(version: string) {
    const data = await fetch(`https://api.papermc.io/v2/projects/paper/versions/${version}/builds`, uaheaderfull).then(r => r.json() as Promise<BuildsBody>)
    return data.builds.findLast(build => build.channel === "default")
}

export async function downloadPaper(to: string) {
    try {
        const root = await mkdir(path.resolve(to, "Paper"), { empty: true })
        await writeEula(root)

        const plugins = await mkdir(path.resolve(root, "plugins"))
        const configs = await mkdir(path.resolve(plugins, "CrowdControl"))
        const config = path.resolve(configs, "config.yml")
        await writeConfig(config)

        console.info("Fetching Paper versions")
        const data = await fetch(`https://api.papermc.io/v2/projects/paper`, uaheaderfull).then(r => r.json() as Promise<ProjectBody>)
        let build: Build | undefined
        let version: string | undefined
        for (const vers of data.versions.toReversed()) {
            if (vers !== "1.21.4") continue; // TODO
            build = await fetchLatest(vers)
            if (build) {
                version = vers
                break
            }
        }

        if (!build || !version) throw new Error("Could not fetch valid Paper version")
        
        console.info(`Downloading Paper ${version}`)
        const serverJar = path.resolve(root, "paper.jar")
        let dlName = build.downloads.application.name
        if (path.extname(dlName) !== ".jar") dlName += ".jar"
        const paperDlUrl = `https://api.papermc.io/v2/projects/paper/versions/${version}/builds/${build.build}/downloads/${dlName}`
        await downloadFile(serverJar, paperDlUrl)

        await writeRun(serverJar, 21)
        
        return await downloadMod(plugins, "paper", version, { filename: "CrowdControl-Paper.jar" })
    } catch (e) {
        console.error("Help", e)
    }
}