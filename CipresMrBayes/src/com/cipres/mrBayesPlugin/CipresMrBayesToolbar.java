package com.cipres.mrBayesPlugin;

import java.util.List;

import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.ngbw.directclient.CiApplication;
import org.ngbw.directclient.CiClient;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.Dialogs.DialogOptions;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.cipres.mrBayesPlugin.models.UserModel;
import com.cipres.mrBayesPlugin.ui.JobManagePanel;
import com.cipres.mrBayesPlugin.ui.LoginOptions;
import com.cipres.mrBayesPlugin.utilities.CipresUtilities;
import com.cipres.mrBayesPlugin.utilities.DataHandlingUtilities;

import jebl.util.ProgressListener;

public class CipresMrBayesToolbar extends DocumentOperation{
	public static CiClient myClient;
	private JPanel displayGuiModel;
	private Boolean newUser = true;
	
	public String getUniqueId(){
		return "Cipres_MrBayes";
	}
	
	@Override
	public GeneiousActionOptions getActionOptions() {
		return new GeneiousActionOptions("Cipres MrBayes").setMainMenuLocation(GeneiousActionOptions.MainMenu.Tools);
		
	}

	@Override
	public String getHelp() {
		return "Getting Help";
	}

	@Override
	public DocumentSelectionSignature[] getSelectionSignatures() {
		return new DocumentSelectionSignature[0];
	}
	
	
	//Geneious will display the Options returned from this method as a panel before calling performOperation().
	public Options getOptions(final AnnotatedPluginDocument[] docs) throws DocumentOperationException{
        Options options = null;
		if(newUser == true){
			options = new LoginOptions();
	        options.canRestoreDefaults();
        }
        
        return options;
    }

    //This is the method that does all the work.  Geneious passes a list of the documents that were selected when the user
    //started the operation, a progressListener, and the options panel that we returned in the getOptionsPanel() method above.
    public List performOperation(AnnotatedPluginDocument[] docs, ProgressListener progress, Options options) throws DocumentOperationException{

    	
    	if(newUser == true){
	    	CiApplication app = CiApplication.getInstance();
	    	DataHandlingUtilities handler = DataHandlingUtilities.getInstance();
	    	
	    	LoginOptions model = (LoginOptions)options;
	        String username = (String)options.getValue("username");
	        String password = model.getPassword();
	        String url = app.getRestUrl();
	        String appName = app.getAppname();
	        String appKey = app.getAppKey();
	        
	        UserModel user = new UserModel(username, password, url, appKey, appName);
	        handler.addUser(user);
	        myClient = handler.getClient();
	        
	        if(CipresUtilities.clientCheck(myClient) == true){
		        JSONArray retJSONArray = null;
				try {
					retJSONArray = CipresUtilities.updateList(myClient, user);
				} catch (ParseException e) {
					e.printStackTrace();
				}
		        displayGuiModel = new JobManagePanel().createPanel(retJSONArray);
	        }
    	}
    	DialogOptions dialogOptions = new DialogOptions(Dialogs.OK_CANCEL, "");
    	if(CipresUtilities.clientCheck(myClient) == true){
	        Dialogs.showDialog(dialogOptions, displayGuiModel);
	        newUser = false;
    	} else{
    		Dialogs.showContinueCancelDialog("Incorrect user information", "Error!", null, Dialogs.DialogIcon.ERROR);
    	}
        
        return null;
   }
    
}
