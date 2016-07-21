#Cipres MrBayes Plugin

##mrBayesPlugin Package

###CipresMrBayesPlugin.java
1. Geneious plugin initiallizer that calls CipresMrBayes
2. Load icons

###CipresMrBayesToolbar.java
1. getActionOption()
 - Determine the placement of the GUI
2. getOptions()
 - Load the option GUI
3. performOperation()
 - Implement code to prompt users to login.
 - Login is located under "Tools" tab named "Cipres MrBayes"

###CipresMrBayesTree.java
1. getActionOption()
 - Determine the placement of the GUI
2. getOption()
 - Load the option GUI
3. performOperation()
 - Implement code to show the options and actions for pre-job submission of a job

##mrBayesPlugin.model Package

###UserModel.java
1. Cipres User model, includes all getter and setter methods

##mrBayesPlugin.ui Package

###JobManagePanel.java
1. Customizable GUI that displays all of the jobs binded to the Cipres user.
2. Currently set to be displayed after user login

###LoginOptions.java
1. User Login GUI
2. Simple GUI that asks for user's username and password to their Cipres account

###MrBayesOptions.java
1. getJsonInterface()
 - Get the input parameters from the interface
2 MrBayesOptions()
 - Constructor of the GUI
 - Determine the interface outline and default values 

##mrBayesPlugin.utilities Package

###CipresUtilities.java
All of these methods are utilizing methods from directclient.jar/package

1. clientCheck()
 - Call myClient.listJobs() to check if there's an exception, if error then client does not exist.
2. listJobs()
 - Add the list of jobs retrieved to the user model
3. getJobs()
 - Get the list of jobs binded to the user account
4. updateList()
 - Update the list of jobs, return the them in a JSON
5. deleteJobs
 - Delte the selected Job
6. submitJob()
 - Submit job to MrBayes's server.

###DataHandlingUtilities.java
1. Getter and Setters for users and their jobs
2. getClient()
 - Fetch the user client from CiClient
3. getUserJSON()
 - Fetch user data in a JSON format, fields of the data can be seened within the method.

###MrBayesUtilities.java
1. Mocking the existing MrBayes plugin
 - Save the input parameter values into a JSON to be sent as a parameter in submitJob()
2. getMetaData()
 - Get the metadata of the user
3. getVParams()
 - Get the param from JSON data

##Geneious Files

Geneious' plugin libraries

##MrBayesPlugin_2_1_3

The example code for the current MrBayes Plugin

##java_umbrelaa

Contains "directclient", Cipres's APIs for client, user and job handling functions.


##Note

All of these code were developed on a Mac, so there may require some configurations if it is to be tested in a different OS.

Useful resources:
1. https://biomatters.zendesk.com/forums/21579261-Plugin-Development-Forum
 - Forum to post questions related to Geneious
 - Click "Add article" instead of submit a request
 - Bookmark the article you posted so you can go back to it easily, there's no history binded to your accounts

2. http://assets.geneious.com/developer/geneious/javadoc/latest/index.html
 - Genious Public API documentation

###Questions I have asked

1. https://biomatters.zendesk.com/entries/98823428-How-to-trigger-a-plugin-by-right-clicking-listed-items

2.https://biomatters.zendesk.com/entries/99219167-How-to-obtain-Sequence-Alignment-file-paths

3.https://biomatters.zendesk.com/entries/100167627-Handling-of-returned-MrBayes-files