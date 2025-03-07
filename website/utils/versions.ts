export interface Version {
    id: string
    latest: boolean
    supported: boolean
}
export interface SpongeVersion extends Version {
    api: number
}

const autoVersion = <T extends Version>(versions: Omit<T, 'latest'>[]): T[] => {
    return [...versions].sort((a, b) => a.id.localeCompare(b.id)).reverse().map((version, index) => ({ ...version, latest: !index } as T))
}
const findLatest = (versions: Version[]): Version => {
    return versions.find(version => version.latest) || versions[0]
}

export const paperVersions: Version[] = autoVersion([
    { id: "1.21.1", supported: true },
    { id: "1.20.6", supported: true },
    { id: "1.20.4", supported: false },
    { id: "1.20.2", supported: false },
    { id: "1.20.1", supported: false },
    { id: "1.19.4", supported: false },
])
export const paperLatest = findLatest(paperVersions)

export const fabricVersions: Version[] = autoVersion([
    { id: "1.21.1", supported: true },
    { id: "1.20.6", supported: false },
    { id: "1.20.4", supported: false },
    { id: "1.20.2", supported: false },
    { id: "1.20.1", supported: false },
    { id: "1.19.4", supported: false },
    { id: "1.19.2", supported: false },
])
export const fabricLatest = findLatest(fabricVersions)

export const neoForgeVersions: Version[] = autoVersion([
    { id: "1.21.1", supported: true },
])
export const neoForgeLatest = findLatest(neoForgeVersions)

// these are all "latest" to allow Automatic Setup to be shown
// (though they're all also unsupported so they won't show up actually)
export const spongeVersions: SpongeVersion[] = [
    { id: "1.21.1", latest: true, api: 12, supported: false, },
    { id: "1.20.6", latest: true, api: 11, supported: false, },
    { id: "1.16.5", latest: true, api: 8, supported: false, },
    { id: "1.12.2", latest: true, api: 7, supported: false, },
]
export const spongeLatest = findLatest(spongeVersions)

const allVersionsSet = new Set([...fabricVersions, ...paperVersions, ...neoForgeVersions, ...spongeVersions].map(version => version.id));
export const allVersions: Version[] = autoVersion([...allVersionsSet].map(version => ({ id: version, supported: false })));

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
