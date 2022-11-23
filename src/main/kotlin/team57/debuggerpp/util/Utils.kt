package team57.debuggerpp.util

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

class Utils {
    companion object {
        // Copied from https://stackoverflow.com/a/42840932
        fun unzipAll(zipInputStream: ZipInputStream, outputPath: Path) {
            zipInputStream.use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val newFilePath = outputPath.resolve(entry.name)
                    if (entry.isDirectory) {
                        Files.createDirectories(newFilePath)
                    } else {
                        if (!Files.exists(newFilePath.parent)) {
                            Files.createDirectories(newFilePath.parent)
                        }
                        Files.newOutputStream(outputPath.resolve(newFilePath)).use { os ->
                            val buffer = ByteArray(Math.toIntExact(entry!!.size))
                            var location: Int
                            while (zis.read(buffer).also { location = it } != -1) {
                                os.write(buffer, 0, location)
                            }
                        }
                    }
                    entry = zis.nextEntry
                }
            }
        }
    }
}