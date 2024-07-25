import path from 'path';
import fs from 'fs-extra';
import { downloadPaper } from './paper.js';
import { downloadFabric } from './fabric.js';
import { downloadSponge } from './sponge.js';
import { downloadJRE } from './java.js';
import { mkdir, semverSort } from './utils.js';

const root = await mkdir(path.resolve("output", "Minecraft"))

await downloadJRE(root, 8)
await downloadJRE(root, 21)

const modVersions = await Promise.allSettled([
    await downloadPaper(root),
    await downloadFabric(root),
    await downloadSponge(root, 7),
    await downloadSponge(root, 8),
    await downloadSponge(root, 11),
])

const modVersion = modVersions
.filter(result => result.status === "fulfilled")
.map(result => result.value.version_number.split("+")[0])
.sort(semverSort)
.at(-1)!

// await fs.writeFile(path.resolve(root, "ccver"), modVersion)