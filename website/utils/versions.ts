export const supportedPaperVersions: string[] = ["1.21.1", "1.20.6"]
export const paperVersions: string[] = ["1.21.1", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4"];
export const paperLatest = paperVersions[0];

export const supportedFabricVersions: string[] = ["1.21.1"];
export const fabricVersions: string[] = ["1.21.1", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.2"];
export const fabricLatest = fabricVersions[0];

export const supportedNeoForgeVersions: string[] = ["1.21.1"];
export const neoForgeVersions: string[] = ["1.21.1"];
export const neoForgeLatest = neoForgeVersions[0];

export const spongeVersions: Map<string, number> = new Map([["1.21.1", 12], ["1.20.6", 11], ["1.16.5", 8], ["1.12.2", 7]]);
export const spongeVersionsArray: string[] = Array.from(spongeVersions.keys());
export const spongeLatest = spongeVersionsArray[0];

const allVersionsSet = new Set(paperVersions.concat(fabricVersions, neoForgeVersions, Array.from(spongeVersions.keys())));
export const allVersions: string[] = Array.from(allVersionsSet).sort().reverse();

const allForgeVersionsSet = new Set(neoForgeVersions.concat(Array.from(spongeVersions.keys())));
export const allForgeVersions: string[] = Array.from(allForgeVersionsSet).sort().reverse();

interface Modloader {
    name: string
    id: string
    versions: string[]
}

export const unsureML = {name: "Unsure", id: "unsure", versions: allVersions} as const satisfies Modloader;
export const paperML = {name: "Paper/Folia", id: "paper", versions: paperVersions} as const satisfies Modloader;
export const fabricML = {name: "Fabric", id: "fabric", versions: fabricVersions} as const satisfies Modloader;
export const neoForgeML = {name: "NeoForge", id: "neoforge", versions: neoForgeVersions} as const satisfies Modloader;
export const spongeML = {name: "Forge/Sponge", id: "sponge", versions: spongeVersionsArray} as const satisfies Modloader;
export const modloaders = [unsureML, paperML, fabricML, neoForgeML, spongeML] as const satisfies Modloader[];