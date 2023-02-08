package team57.debuggerpp.util

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
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

        @JvmStatic
        fun findClassName(project: Project, file: VirtualFile, offset: Int): String? {
            return ReadAction.compute<String?, Throwable> {
                PsiTreeUtil.getParentOfType(
                    PsiManager.getInstance(project).findFile(file)?.findElementAt(offset),
                    PsiClass::class.java
                )?.qualifiedName
            }
        }

        @JvmStatic
        fun findClassName(file: PsiFile, offset: Int): String? {
            return ReadAction.compute<String?, Throwable> {
                PsiTreeUtil.getParentOfType(
                    file.findElementAt(offset),
                    PsiClass::class.java
                )?.qualifiedName
            }
        }
    }
}