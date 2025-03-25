import path from "path";
import fs from "fs-extra";
import { downloadFile, jrePaths, mkdir, uaheaderfull, writeConfig, writeEula, writeRun } from "./utils.js";
import { downloadMod } from "./modrinth.js";
import child_process from "child_process";

interface NeoVersions {
    isSnapshot: boolean
    versions: string[]
}

export async function downloadNeoForge(to: string) {
    const root = await mkdir(path.resolve(to, `NeoForge`), { empty: true })
    await writeEula(root)

    const configs = await mkdir(path.resolve(root, "config"))
    const config = path.resolve(configs, "crowdcontrol.conf")
    await writeConfig(config)

    const neoVersions = await fetch("https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge", uaheaderfull).then(r => r.json()) as NeoVersions
    const neoLatest = neoVersions.versions.reverse().find(ver => ver.startsWith("21.4.") && !ver.includes("beta"))! // TODO
    const minecraft = `1.${neoLatest.substring(0, 4)}` // TODO

    console.info(`Downloading NeoForge ${neoLatest} installer`)
    const installerJar = path.resolve(to, `neoforge-${neoLatest}.jar`)
    const installerUrl = `https://maven.neoforged.net/releases/net/neoforged/neoforge/${neoLatest}/neoforge-${neoLatest}-installer.jar`
    await downloadFile(installerJar, installerUrl)

    console.info(`Running NeoForge ${neoLatest} installer`)
    const child = child_process.spawn("java", ["-jar", installerJar, "--installServer"], {
        cwd: root,
    })
    for await (const data of child.stdout) {
        // no-op (this is the only way to block until the installer finishes)
    }
    await fs.unlink(installerJar)

    const jre = jrePaths[21]
    const batFile = path.resolve(root, "run.bat")
    await fs.writeFile(batFile, `@echo off
start ..\\java\\${jre}\\bin\\java.exe -Xmx2048M -Xms2048M @user_jvm_args.txt @libraries/net/neoforged/neoforge/${neoLatest}/win_args.txt %* > log.txt 2> errorlog.txt`)

    const mods = path.resolve(root, "mods")
    await mkdir(mods, { empty: true })
    return await downloadMod(mods, "neoforge", minecraft, { filename: `CrowdControl-NeoForge.jar` })
}