package mrbayes.plugin;

import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import org.suchard.gui.posterior.*;

import java.io.File;

/**
 * @author Marc Suchard
 * @version $Id: MrBayesPlugin.java 70139 2015-10-13 22:46:59Z sidney $
 */
public class MrBayesPlugin extends GeneiousPlugin {

	public String getName() {
		//new MersenneTwister(1);
		return "MrBayes Plugin";
	}

	public String getDescription() {
		return "Perform simple posterior simulations using MrBayes";
	}

	public String getHelp() {
		return null;
	}

	public String getAuthors() {
		return "Marc Suchard and Biomatters Ltd";
	}

    /**
     * 0.4: Changed default subsampling frequency to 200, added warning when
     * number of samples > 6000, added duplicate name checking and automatic
     * renaming of duplicates
     *
     * 0.5: Geneious 3.5 release version. Fixed several bugs.
     * 0.7: Geneious 3.6 release (api broken)
     * 0.8 released 2008-05-06
     *  - Added protein rate matrix option
     *  - Geneious 3.7 release.
     * 0.9 released 2008-09-18
     *  - Better progress and memory handling
     *  - Support for grid technology
     *  - Allow the user to specify a custom command block
     *  - Chain length etc is no longer in multiples of 1000
     *  - Various minor bug fixes and tweaks
     * 0.9.1 released 2008-09-22
     *  - Supports user trees in the custom command block
     * 0.9.2 released 2008-10-07
     * - Imports a greater range of results from custom command block runs
     * - Doesn't ignore rate settings
     * 1.0 released 2009-03-23
     * - Supports alignment workflows via ClustalW and/or Green Button
     * - Can be stopped at any stage and results will be imported
     * 1.0.1 released 2009-04-02
     * - Doesn't depend on warning16.png in resources folder (not present in older versions of Geneious)
     *     1.0.2 released 2009-05-04
     *     exactly the same as 0.9.2- just needed 1.0.1 users to be told to upgrade due to GEN-7338
     * 1.1 releases 2009-04-24
     * Fixes for clustal/green button
     *     1.1.0.1 released 2009-05-04
     *     Same as 1.1.1 but with JEditorPanes so it's compatible with 4.5
     * 1.1.1 released 2009-05-04
     * Previous version should have said it was compatible with 4.6 and later
     * because it uses GEditorPane
     * 2.0 beta release 2009-12-22, full release 2010-01-18
     * Fix progress before first iteration completes, don't crash if Finish Now clicked before first iteration complete,
     * don't multiply values by 1000 when verifying options, add mrbayes to Tree dialog, greatly improved handling of a variety
     * of outputs (partitioning and parallel runs), added time estimation to progress message, added outgroup to basic options,
     * raw trees now rooted correctly, trprobs file now imported (sorted topologies), now runs on 64bit linux, average standard
     * deviation of split frequencies now displayed in progress, if an error occurs during a run then the partian results
     * can be imported
     * 2.0.1 limited release 2010-01-19
     * Remove check for less than 1000 samples
     * 2.0.2 2010-03-3
     * Fix problem importing Green Button results
     * NOTE: - Second private beta release on 16 August 2010 with Geneious Server
     * 2.0.3 2010-09-14
     * Made non-backwards compatible changes to GeneiousGrid* classes
     * NOTE: - First public beta release on 21  September 2010 with Geneious Server
     *
     * 2.0.4 2011-05-31
     * - Fixed a 1 in 32,000 chance to crash, see GEN-12925
     * - Fixed an issue with meta data in trees, see GEN-13704
     * 2.0.5 2012/07/13
     * - GEN-15118 uses new version of DocumentUtilities.getUniqueNameForDocument() added in 5.5.1
     * - GEN-15145 MrBayes tries to open a dialog on Geneious Server
     * 2.0.6 2012.11.07
     * - GEN-18106 MrBayes runs out of memory on alignment with too many sequences (group 6338)
     * - Implements OldVersionCompatible
     * - GEN-18787 MrBayesOptions crashes on duplicate names in alignment (group 6634)
     * 2.0.7 2013.01.23
     * - Executable updated to 3.2.1
     * - GEN-19308 Make GreenButton work with the new MrBayes
     * 2.0.8 2013.07.01
     * - GEN-20251 plugin doesn't import consensus tree produced by mrbayes
     * - GEN-20265 New MrBayes consensus tree has non-string attribute values?
     * - GEN-20355 MrBayes consensus tree has stupid tip attributes
     * - GEN-20538 Two MrBayes document types that had same name crashed workflows - make one type
     * 2.0.9 2013.10.24
     *   GEN-20707 Cancelling the operation in Geneious did not quit the running MrBayes process on Mac and Linux
     * 2.0.10 (sent to one customer 28 Jul 2014)
     * - A 6.1 build of the plugin with GEN-22861 fix
     * 2.1.0 2014.06.19
     * - Executables updated to 3.2.2
     * - GEN-19868 Removing traces that can't be initialized/analyzed because of infinite values (produced by previous version of MrBayes on Windows)
     * - Changed option treeagepr = exponential(int) to ~ = gamma(<alpha>, <beta>) to reflect changes in executables
     * - GEN-22377 Can now cancel the operation (fixed in their binaries)
     * 2.1.1 2014.06.21
     * - GEN-22710 MrBayes 2.1.0 minimum api version is wrong (group 9091)
     * 2.1.2 (sent to one customer 28 Jul 2014)
     * - GEN-22861 MrBayes originalAlignment file is missing at the end of a huge run
     * 2.1.3 2015.10.14
     * - GEN-24504 MrBayes can lose files during long runs by using system temp files that are subject to deletion
     * - GEN-23986 Tree Viewer - improve performance on huge trees (switch outgroup option to string option when more than 10,000 seqs in alignment
     */
    public String getVersion() {
		return "2.1.3";
	}

    @Override
    public int getMaximumApiVersion() {
        return 4;
    }

    @Override
    public String getMinimumApiVersion() {
        return "4.701";
    }

	@Override
	public DocumentOperation[] getDocumentOperations() {
		return new DocumentOperation[]{new MrBayesDocumentOperation(true),
                new MrBayesDocumentOperation(false)};
	}

    @Override
	public DocumentViewerFactory[] getDocumentViewerFactories() {
		return new DocumentViewerFactory[]{
				new CorrelationViewerFactory(),
				new DensityViewerFactory(),
				new RawTraceViewerFactory(),
				new SummaryStatisticsViewerFactory(),
                new PosteriorOutputTextViewer()
        };
	}

	@Override
	public DocumentType[] getDocumentTypes() {
		return new DocumentType[]{
				new DocumentType("MrBayes Posterior Output", PosteriorDocument.class, documentIcons),
        };
	}

    public DocumentFileImporter[] getDocumentFileImporters() {
        return new DocumentFileImporter[] {new MrBayesPosteriorImporter()};
    }

    public GeneiousPreferences getPreferences() {
		return null;
	}

	private File pluginDirectory;
	static Icons documentIcons;
	static Icons toolbarIcons;
    static Icons lightbulbIcon;
    static Icons warningIcon;

	public void initialize(File INpluginUserDirectory, File pluginDirectory) {
        this.pluginDirectory = pluginDirectory;

        String posterior16Loc = pluginDirectory.getAbsolutePath() + File.separator + "posterior16.png";
        String posterior32Loc = pluginDirectory.getAbsolutePath() + File.separator + "posterior32.png";
        String posterior24Loc = pluginDirectory.getAbsolutePath() + File.separator + "posterior24.png";
        String lightbulb16Loc = pluginDirectory.getAbsolutePath() + File.separator + "lightbulb16.png";
        String warning16Loc = pluginDirectory.getAbsolutePath() + File.separator + "warning.png";
        if (new File(posterior16Loc).exists()) { //running from IntelliJ
            documentIcons = IconUtilities.getIcons(posterior16Loc, posterior32Loc);
            toolbarIcons = IconUtilities.getIcons(posterior16Loc, posterior24Loc);
            lightbulbIcon = IconUtilities.getIcons(lightbulb16Loc);
            warningIcon = IconUtilities.getIcons(warning16Loc);
        } else {
            documentIcons = IconUtilities.getIconsFromJar(getClass(), "/posterior16.png", "/posterior32.png");
            toolbarIcons = IconUtilities.getIconsFromJar(getClass(), "/posterior16.png", "/posterior24.png");
            lightbulbIcon = IconUtilities.getIconsFromJar(getClass(), "/lightbulb16.png");
            warningIcon = IconUtilities.getIconsFromJar(getClass(), "/warning.png");
        }
    }
}
