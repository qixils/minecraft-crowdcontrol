export let paperVersions: string[] = ["1.20.1", "1.19.4"];
export let fabricVersions: string[] = ["1.20.1", "1.19.4", "1.19.2"];
export let sponge8Versions: string[] = ["1.16.5"];
export let sponge7Versions: string[] = ["1.12.2"];

let allVersionsSet = new Set(paperVersions.concat(fabricVersions, sponge8Versions, sponge7Versions));
export let allVersions: string[] = Array.from(allVersionsSet).sort().reverse();