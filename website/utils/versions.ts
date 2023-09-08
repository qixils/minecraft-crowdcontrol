export const paperVersions: string[] = ["1.20.1", "1.19.4"];
export const paperLatest = paperVersions[0];
export const fabricVersions: string[] = ["1.20.1", "1.19.4", "1.19.2"];
export const fabricLatest = fabricVersions[0];
export const spongeVersions: Map<string, number> = new Map([["1.16.5", 8], ["1.12.2", 7]]);
export const spongeVersionsArray: string[] = Array.from(spongeVersions.keys());
export const spongeLatest = spongeVersionsArray[0];

const allVersionsSet = new Set(paperVersions.concat(fabricVersions, Array.from(spongeVersions.keys())));
export const allVersions: string[] = Array.from(allVersionsSet).sort().reverse();