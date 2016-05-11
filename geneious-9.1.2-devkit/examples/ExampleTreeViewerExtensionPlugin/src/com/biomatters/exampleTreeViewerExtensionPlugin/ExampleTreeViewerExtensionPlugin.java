package com.biomatters.exampleTreeViewerExtensionPlugin;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.TreeViewerExtension;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;

public class ExampleTreeViewerExtensionPlugin extends GeneiousPlugin {
    public TreeViewerExtension.Factory[] getTreeViewerExtensionFactories() {
        TreeViewerExtension.Factory factory = new TreeViewerExtension.Factory() {
            public TreeViewerExtension createTreeViewerExtension(DocumentViewer viewer, AnnotatedPluginDocument annotatedPluginDocument) {
                return new ExampleTreeViewerExtension(annotatedPluginDocument);
            }
        };
        return new TreeViewerExtension.Factory[]{factory};
    }

    public String getName() {
        return "ExampleTreeViewerExtensionPlugin";
    }

    public String getHelp() {
        return "ExampleTreeViewerExtensionPlugin";
    }

    public String getDescription() {
        return "ExampleTreeViewerExtensionPlugin";
    }

    public String getAuthors() {
        return "Biomatters";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getMinimumApiVersion() {
        return "4.1";
    }

    public int getMaximumApiVersion() {
        return 4;
    }
}
