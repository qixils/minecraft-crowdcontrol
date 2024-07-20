import path from "path";
import fs from "fs-extra";
import { downloadFile, jreMode, jrePaths, mkdir, uaheaderfull } from "./utils.js";
import AdmZip from "adm-zip";

type Download = {
    checksum: string
    checksum_link: string
    download_count: number
    link: string
    metadata_link: string
    name: string
    signature_link: string
    size: number
}

type AssetsBody = {
    binary: {
        architecture: string
        download_count: number
        heap_size: string
        image_type: string
        installer: Download
        jvm_impl: string
        os: string
        package: Download
        project: string
        scm_ref: string
        updated_at: string
    }
    release_link: string
    release_name: string
    vendor: string
    version: {
        build: number
        major: number
        minor: number
        openjdk_version: string
        optional: string
        security: number
        semver: string
    }
}[]

export async function downloadJRE(to: string, version: keyof (typeof jrePaths)) {
    console.info(`Fetching Java ${version} versions`)
    const apiurl = `https://api.adoptium.net/v3/assets/latest/${version}/hotspot?architecture=x64&image_type=jre&os=windows&vendor=eclipse`
    const data = (await fetch(apiurl, uaheaderfull).then(r => r.json() as Promise<AssetsBody>))[0];

    if (jreMode === "dynamic")
        jrePaths[version] = data.release_name.replaceAll("+", "-")

    const outpath = await mkdir(path.resolve(to, "java", jrePaths[version]));
    const releasepath = path.resolve(outpath, "release");
    let semver: string | undefined;
    if (fs.existsSync(releasepath)) {
        let info = fs.readFileSync(releasepath, "utf8");
        semver = info.match(/SEMANTIC_VERSION="(.*)"/)?.[1];
    }

    if (data.version.semver === semver) {
        console.info("Already up-to-date, skipping download")
        return;
    }
    await fs.emptyDir(outpath)

    console.info(`Downloading Java ${data.version.semver}`)
    const url = data.binary.package.link;
    const zippath = path.resolve(outpath, "jre.zip");
    await downloadFile(zippath, url)

    console.info(`Extracting Java ${version}`)
    const zip = new AdmZip(zippath);
    const entries = zip.getEntries()
    const subfolder = entries[0].entryName

    for (const entry of entries.slice(1, -1)) {
        if (entry.isDirectory) continue
        const relative = entry.entryName.substring(subfolder.length)
        const filepath = path.resolve(outpath, ...relative.split("/"))
        const dirpath = path.dirname(filepath)
        await mkdir(dirpath)
        zip.extractEntryTo(entry, dirpath, false, true)
    }

    fs.unlinkSync(zippath);
}
