const path = require('path');
const fs = require('fs');
const AdmZip = require('adm-zip');

const ua = "qixils/minecraft-crowdcontrol/1.0.0";
const uaheader = {"User-Agent": ua};
const uaheaderfull = {"headers": uaheader};

function isSemverGE(newsemver, oldsemver) {
    let newparts = newsemver.split(".");
    let oldparts = oldsemver.split(".");
    let parts = Math.max(newparts.length, oldparts.length);
    for (let i = 0; i < parts; i++) {
        let newpart = parseInt(newparts[i] || "0");
        let oldpart = parseInt(oldparts[i] || "0");
        if (newpart > oldpart) {
            return true;
        } else if (newpart < oldpart) {
            return false;
        }
    }
    return true;
}

function mkdir(dir) {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir);
    }
    return dir;
}

// Get path to root download folder
let appdata = process.env.appdata;
let ccapps = mkdir(path.resolve(appdata, "CrowdControl-Apps"));
let root = mkdir(path.resolve(ccapps, "Minecraft"));

// Download JREs
let jres = mkdir(path.resolve(root, "java"));
async function downloadJRE(version) {
    let outpath = mkdir(path.resolve(jres, "jre" + version));
    let releasepath = path.resolve(outpath, "release");
    let semver = undefined;
    if (fs.existsSync(releasepath)) {
        let info = fs.readFileSync(releasepath, "utf8");
        semver = info.match(/SEMANTIC_VERSION="(.*)"/)[1];
    }
    let apiurl = `https://api.adoptium.net/v3/assets/latest/${version}/hotspot?architecture=x64&image_type=jre&os=windows&vendor=eclipse`
    let data = await fetch(apiurl, uaheaderfull).then(r => r.json());
    data = data[0];
    if (data.version.semver === semver) {
        return;
    }
    let url = data.binary.package.link;
    let zippath = path.resolve(outpath, "jre.zip");
    let file = fs.createWriteStream(zippath);
    await fetch(url, uaheaderfull).then(r => r.body.pipeTo(file));
    file.close();
    let zip = new AdmZip(zippath);
    zip.extractAllTo(outpath, true);
    fs.unlinkSync(zippath);
}
await downloadJRE("8");
await downloadJRE("17");

// Prepare Mod Downloads
async function downloadMod(destination, platform, suffix) {
    suffix = suffix || platform;
    let apiurl = `https://api.modrinth.com/v2/project/crowdcontrol/version?loaders=["${platform}"]`;
    let data = await fetch(apiurl, uaheaderfull).then(r => r.json());
    let latestversion, latestmodversion, latestgameversion;
    for (let version of data) {
        let versionnumber, modversion, gameversion = version.version_number.match(/([0-9.]+)\+/ + suffix + /(?:-([0-9.]+))?/);
        if (!versionnumber) continue;
        if (!isSemverGE(modversion, latestmodversion)) continue;
        if (latestgameversion && (!gameversion || !isSemverGE(gameversion, latestgameversion))) continue;
        latestversion = version;
        latestmodversion = modversion;
        latestgameversion = gameversion;
    }
    latestgameversion = latestgameversion || latestversion.game_versions[-1];
    let url = latestversion.files[0].url;
    let jarpath = path.resolve(destination, "CrowdControl.jar");
    let file = fs.createWriteStream(jarpath);
    await fetch(url, uaheaderfull).then(r => r.body.pipeTo(file));
    file.close();
    return latestmodversion, latestgameversion;
}

// Paper
let paper = mkdir(path.resolve(root, "Paper"));
let paperplugins = mkdir(path.resolve(paper, "plugins"));
let ccver, paperversion = await downloadMod(paperplugins, "paper");
let paperurl = `https://api.papermc.io/v2/projects/paper/versions/${paperversion}/builds`;
let paperdata = await fetch(paperurl, uaheaderfull).then(r => r.json());
let paperbuild = paperdata.builds[-1].build;
let paperjar = path.resolve(paper, "paper.jar");
let paperfile = fs.createWriteStream(paperjar);
let paperdlurl = `https://papermc.io/api/v2/projects/paper/versions/${paperversion}/builds/${paperbuild}/downloads/paper-${paperversion}-${paperbuild}.jar`;
await fetch(paperdlurl, uaheaderfull).then(r => r.body.pipeTo(paperfile));
paperfile.close();

// Fabric
let fabric = mkdir(path.resolve(root, "Fabric"));
let fabricmods = mkdir(path.resolve(fabric, "mods"));
let fabricver, fabricversion = await downloadMod(fabricmods, "fabric");
if (isSemverGE(fabricver, ccver)) {
    ccver = fabricver;
}
let fabricurl = `https://meta.fabricmc.net/v2/versions/loader/${fabricversion}`; // todo