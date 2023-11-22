export const paperVersions: string[] = ["1.20.2", "1.20.1", "1.19.4"];
export const paperLatest = paperVersions[0];
export const supportedFabricVersions: string[] = ["1.20.3"];
export const fabricVersions: string[] = ["1.20.3", "1.20.2", "1.20.1", "1.19.4", "1.19.2"];
export const fabricLatest = fabricVersions[0];
export const spongeVersions: Map<string, number> = new Map([["1.16.5", 8], ["1.12.2", 7]]);
export const spongeVersionsArray: string[] = Array.from(spongeVersions.keys());
export const spongeLatest = spongeVersionsArray[0];

const allVersionsSet = new Set(paperVersions.concat(fabricVersions, Array.from(spongeVersions.keys())));
export const allVersions: string[] = Array.from(allVersionsSet).sort().reverse();

interface Modloader {
    name: string
    id: string
    versions: string[]
}

export const unsureML: Modloader = {name: "Unsure", id: "unsure", versions: allVersions};
export const paperML: Modloader = {name: "Paper", id: "paper", versions: paperVersions};
export const fabricML: Modloader = {name: "Fabric", id: "fabric", versions: fabricVersions};
export const spongeML: Modloader = {name: "Sponge/Forge", id: "sponge", versions: spongeVersionsArray};
export const modloaders: Modloader[] = [unsureML, paperML, fabricML, spongeML];