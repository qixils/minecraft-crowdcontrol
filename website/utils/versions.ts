import type { SetOptional } from "type-fest"

// todo: improve this some day
export const sortIds: Record<string, string> = {
    "25w14craftmine": "1.21.05-AF",
    "1.21.9": "1.21.09",
    "1.21.8": "1.21.08",
    "1.21.7": "1.21.07",
    "1.21.6": "1.21.06",
    "1.21.5": "1.21.05",
    "1.21.4": "1.21.04",
    "1.21.3": "1.21.03",
    "1.21.2": "1.21.02",
    "1.21.1": "1.21.01",
    "1.21.0": "1.21.00",
    "1.21": "1.21.00",
}

export interface Version {
    id: string
    latest: boolean
    supported: boolean
    legacy: boolean
}
export interface SpongeVersion extends Version {
    api: number
}

const autoVersion = <T extends Version>(versions: SetOptional<T, 'latest'>[]): T[] => {
    return [...versions].sort((a, b) => (sortIds[a.id] || a.id).localeCompare(sortIds[b.id] || b.id)).reverse().map((version, index) => ({ latest: !index, ...version } as unknown as T))
}
const findLatest = (versions: Version[]): Version => {
    return versions.find(version => version.latest) || versions[0]
}

export const paperVersions: Version[] = autoVersion([
    { id: "1.21.11", supported: true, legacy: false, latest: false },
    { id: "1.21.10", supported: true, legacy: false, latest: true },
    { id: "1.21.8", supported: true, legacy: false },
    { id: "1.21.6", supported: true, legacy: false },
    { id: "1.21.5", supported: true, legacy: false },
    { id: "1.21.4", supported: true, legacy: false },
    { id: "1.21.3", supported: true, legacy: false },
    { id: "1.21.1", supported: false, legacy: true },
    { id: "1.20.6", supported: false, legacy: true },
    { id: "1.20.4", supported: false, legacy: true },
    { id: "1.20.2", supported: false, legacy: true },
    { id: "1.20.1", supported: false, legacy: true },
    { id: "1.19.4", supported: false, legacy: true },
])
export const paperLatest = findLatest(paperVersions)

export const fabricVersions: Version[] = autoVersion([
    { id: "1.21.11", supported: true, legacy: false },
    { id: "1.21.10", supported: true, legacy: false },
    { id: "1.21.8", supported: true, legacy: false },
    { id: "1.21.6", supported: true, legacy: false },
    { id: "25w14craftmine", supported: true, legacy: false },
    { id: "1.21.5", supported: true, legacy: false },
    { id: "1.21.4", supported: false, legacy: false },
    { id: "1.21.3", supported: false, legacy: false },
    { id: "1.21.1", supported: false, legacy: true },
    { id: "1.20.6", supported: false, legacy: true },
    { id: "1.20.4", supported: false, legacy: true },
    { id: "1.20.2", supported: false, legacy: true },
    { id: "1.20.1", supported: false, legacy: true },
    { id: "1.19.4", supported: false, legacy: true },
    { id: "1.19.2", supported: false, legacy: true },
])
export const fabricLatest = findLatest(fabricVersions)

export const neoForgeVersions: Version[] = autoVersion([
//     { id: "1.21.8", supported: true, legacy: false },
    { id: "1.21.6", supported: true, legacy: false },
    { id: "25w14craftmine", supported: true, legacy: false },
    { id: "1.21.5", supported: true, legacy: false },
    { id: "1.21.4", supported: false, legacy: false },
    { id: "1.21.3", supported: false, legacy: false },
    { id: "1.21.1", supported: false, legacy: true },
])
export const neoForgeLatest = findLatest(neoForgeVersions)

// these are all "latest" to allow Automatic Setup to be shown
// (though they're all also unsupported so they won't show up actually)
export const spongeVersions: SpongeVersion[] = [
    { id: "1.21.1", latest: true, api: 12, supported: false, legacy: true },
    { id: "1.20.6", latest: true, api: 11, supported: false, legacy: true },
    { id: "1.16.5", latest: true, api: 8, supported: false, legacy: true },
    { id: "1.12.2", latest: true, api: 7, supported: false, legacy: true },
]
export const spongeLatest = findLatest(spongeVersions)

const allVersionsSet = new Set([...fabricVersions, ...paperVersions, ...neoForgeVersions, ...spongeVersions].map(version => version.id));
export const allVersions: Version[] = autoVersion([...allVersionsSet].map(version => ({ id: version, supported: false, legacy: false })));

export interface Modloader<T extends Version = Version> {
    name: string
    id: string
    versions: T[]
}

export interface SpongeModloader extends Modloader<SpongeVersion> {}

export const unsureML = {name: "Unsure, pick for me!", id: "unsure", versions: allVersions} as const satisfies Modloader;
export const paperML = {name: "Paper/Folia", id: "paper", versions: paperVersions} as const satisfies Modloader;
export const fabricML = {name: "Fabric", id: "fabric", versions: fabricVersions} as const satisfies Modloader;
export const neoForgeML = {name: "NeoForge", id: "neoforge", versions: neoForgeVersions} as const satisfies Modloader;
export const spongeML = {name: "Forge/Sponge", id: "sponge", versions: spongeVersions} as const satisfies SpongeModloader;
export const modloaders = [unsureML, paperML, fabricML, neoForgeML, spongeML] as const satisfies Modloader[];
export const modloadersBySlug = Object.fromEntries(modloaders.map(({ id, ...modloader }) => [id, modloader] as const))
