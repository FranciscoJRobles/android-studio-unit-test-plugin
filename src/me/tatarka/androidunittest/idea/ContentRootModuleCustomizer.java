package me.tatarka.androidunittest.idea;

import com.android.builder.model.JavaArtifact;
import com.android.builder.model.SourceProvider;
import com.google.common.collect.Lists;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by evan on 6/3/14.
 */
public class ContentRootModuleCustomizer extends AbstractContentRootModuleCustomizer<IdeaAndroidUnitTest> {
    private JavaArtifact mySelectedTestJavaArtifact;

    @NotNull
    @Override
    protected Collection<ContentEntry> findOrCreateContentEntries(@NotNull ModifiableRootModel model, @NotNull IdeaAndroidUnitTest androidUnitTest) {
        VirtualFile rootDir = androidUnitTest.getRootDir();
        File rootDirPath = VfsUtilCore.virtualToIoFile(rootDir);

        List<ContentEntry> contentEntries = Lists.newArrayList(model.addContentEntry(rootDir));
        File buildFolderPath = androidUnitTest.getAndroidDelegate().getBuildFolder();
        if (!FileUtil.isAncestor(rootDirPath, buildFolderPath, false)) {
            contentEntries.add(model.addContentEntry(FilePaths.pathToIdeaUrl(buildFolderPath)));
        }

        JavaArtifact selectedTestJavaArtifact = androidUnitTest.getSelectedTestJavaArtifact(model.getModule());
        mySelectedTestJavaArtifact = selectedTestJavaArtifact;

        if (selectedTestJavaArtifact != null) {
            setCompilerOutputPath(model, selectedTestJavaArtifact.getClassesFolder(), true);
        }

        return contentEntries;
    }

    @Override
    protected void setUpContentEntries(@NotNull Collection<ContentEntry> contentEntries, @NotNull IdeaAndroidUnitTest androidUnitTest, @NotNull List<RootSourceFolder> orphans) {
        if (mySelectedTestJavaArtifact == null) {
            return;
        }

        SourceProvider sourceProvider = mySelectedTestJavaArtifact.getVariantSourceProvider();

        Collection<File> testSources = sourceProvider.getJavaDirectories();
        for (File source : testSources) {
            addSourceFolder(contentEntries, source, JavaSourceRootType.TEST_SOURCE, false, orphans);
        }
        Collection<File> testResources = sourceProvider.getResourcesDirectories();
        for (File resource : testResources) {
            addSourceFolder(contentEntries, resource, JavaResourceRootType.TEST_RESOURCE, false, orphans);
        }
    }
}
