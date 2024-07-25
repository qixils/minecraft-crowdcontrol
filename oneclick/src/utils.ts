import type { Writable } from "node:stream";
import { ReadableStream } from "node:stream/web";
import fs from "fs-extra";
import path from "node:path";
import axios from "axios";

export const ua = "qixils/minecraft-crowdcontrol/1.0.0";
export const uaheader = { "User-Agent": ua };
export const uaheaderfull = { "headers": uaheader };

export const jreMode: "static" | "dynamic" = "dynamic"
export const jrePaths = {
  8: "jre8",
  21: "jre21",
}

export async function mkdir(dir: string, {
  empty = false,
}: {
  empty?: boolean
} = {}): Promise<string> {
  await fs.ensureDir(dir)
  if (empty) await fs.emptyDir(dir);
  return dir;
}

export function isSemverGE(newsemver: string, oldsemver: string): boolean {
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

export function semverSort(a: string, b: string): -1 | 0 | 1 {
  if (a == b) return 0
  if (isSemverGE(a, b)) return -1
  if (isSemverGE(b, a)) return 1
  return 0 // fallback
}

// From https://github.com/remix-run/remix/blob/58ac1e9/packages/remix-node/stream.ts#L4-L29
// Licensed under MIT
export async function writeReadableStreamToWritable(
  stream: ReadableStream,
  writable: Writable
) {
  let reader = stream.getReader();
  let flushable = writable as { flush?: Function };

  try {
    while (true) {
      let { done, value } = await reader.read();

      if (done) {
        writable.end();
        break;
      }

      writable.write(value);
      if (typeof flushable.flush === "function") {
        flushable.flush();
      }
    }
  } catch (error: unknown) {
    writable.destroy(error as Error);
    throw error;
  }
}

export async function downloadFile(
  path: fs.PathLike,
  url: string,
) {
  const { data } = await axios.get(url, { responseType: 'stream' });
    
  // Pipe the data to a file
  const writeStream = fs.createWriteStream(path);
  data.pipe(writeStream);

  // Return a promise and resolve when download finishes
  return new Promise((resolve, reject) => {
      data.on('error', () => {
          reject(`Failure while retrieving remote data (source: ${url})`);
      })

      writeStream.on('close', () => {
          resolve(path);
      })
      writeStream.on('error', err => {
          reject(err);
      })
  })
}

export async function writeEula(
  folder: string,
) {
  const file = path.resolve(folder, "eula.txt")
  const contents = `eula=true`
  await fs.writeFile(file, contents)
}

export async function writeConfig(
  file: string,
) {
  const source = "config" + path.extname(file)
  const sourceFile = path.resolve("static", source)
  await fs.copyFile(sourceFile, file)
}

export async function writeRun(
  jar: string,
  java: keyof (typeof jrePaths),
) {
  const jre = jrePaths[java]
  const jarName = path.basename(jar)
  const batFile = path.resolve(path.dirname(jar), "run.bat")
  await fs.writeFile(batFile, `@echo off
start ..\\java\\${jre}\\bin\\java.exe -Xmx2048M -Xms2048M -jar ${jarName} nogui > log.txt 2> errorlog.txt`)
}
