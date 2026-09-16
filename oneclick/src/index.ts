import path from 'path';
import {downloadPaper} from './paper.js';
import {downloadFabric} from './fabric.js';
import {downloadJRE} from './java.js';
import {mkdir} from './utils.js';
import {downloadNeoForge} from './neoforge.js';

const root = await mkdir(path.resolve("output", "Minecraft"))

// await downloadJRE(root, 8)
// await downloadJRE(root, 21)
await downloadJRE(root, 25)

const modVersions = await Promise.allSettled([
    downloadPaper(root, "26.2", 25),
    downloadFabric(root, "26.3", 25),
    downloadNeoForge(root, "26.3", 25),
])

modVersions
    .map(result => result.status === 'fulfilled' ? (result.value?.version_number || 'n/a?') : result.reason)
    .forEach(modVersion => console.log(modVersion))
for (const v of modVersions) {
    if (v.status !== 'rejected') continue
    console.error('!!! something failed !!!', v.reason)
}

// await fs.writeFile(path.resolve(root, "ccver"), modVersion)