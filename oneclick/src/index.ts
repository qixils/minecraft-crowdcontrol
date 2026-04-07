import path from 'path';
import { downloadPaper } from './paper.js';
import { downloadFabric } from './fabric.js';
import { downloadJRE } from './java.js';
import { mkdir, semverSort } from './utils.js';
import { downloadNeoForge } from './neoforge.js';

const root = await mkdir(path.resolve("output", "Minecraft"))

// await downloadJRE(root, 8)
await downloadJRE(root, 21)
await downloadJRE(root, 25)

const modVersions = await Promise.allSettled([
    downloadPaper(root, "1.21.11", 21),
    downloadFabric(root, "26.1.1", 25),
    downloadNeoForge(root, "26.1.1", 25),
])

const modVersion = modVersions
.flatMap(result => (result.status === 'fulfilled' && !!result.value) ? [result.value] : [])
.map(result => result.version_number.split("+")[0])
.sort(semverSort)
.at(-1)!

console.log(modVersion)
for (const v of modVersions) {
    if (v.status !== 'rejected') continue
    console.error('!!! something failed !!!', v.reason)
}

// await fs.writeFile(path.resolve(root, "ccver"), modVersion)