import path from 'path';
import { downloadFile } from "./utils.js";
import { ModrinthV2Client } from '@xmcl/modrinth'

const client = new ModrinthV2Client()

// Prepare Mod Downloads
export async function downloadMod(destinationFolder: string, platform: string, gameVersion: string, {
    project = "crowdcontrol",
    filename = "CrowdControl.jar",
}: {
    project?: string,
    filename?: string,
} = {}) {
    console.info(`Downloading mod ${project} for ${platform} ${gameVersion}`)

    const versions = await client.getProjectVersions(project, { loaders: [platform] })
    const version = versions.find(ver => ver.game_versions.includes(gameVersion))
    if (!version) throw new Error(`No version found for ${platform} ${gameVersion}`)

    const { url } = version.files[0];
    const jarPath = path.resolve(destinationFolder, filename);
    await downloadFile(jarPath, url)

    return version;
}