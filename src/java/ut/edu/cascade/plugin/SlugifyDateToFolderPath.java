
package ut.edu.cascade.plugin;


import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.cms.assetfactory.BaseAssetFactoryPlugin;
import com.cms.assetfactory.FatalPluginException;
import com.cms.assetfactory.PluginException;
import com.hannonhill.cascade.api.asset.admin.AssetFactory;
import com.hannonhill.cascade.api.asset.common.BaseAsset;
import com.hannonhill.cascade.api.asset.common.Identifier;
import com.hannonhill.cascade.api.asset.home.Folder;
import com.hannonhill.cascade.api.asset.home.FolderContainedAsset;
import com.hannonhill.cascade.api.asset.home.Page;
import com.hannonhill.cascade.api.operation.Read;
import com.hannonhill.cascade.api.operation.result.ReadOperationResult;
import com.hannonhill.cascade.model.dom.identifier.EntityTypes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * This is a simple asset factory plugin.
 */
public final class SlugifyDateToFolderPath extends BaseAssetFactoryPlugin
{
    /** The resource bundle key for the name of the plugin */
    private static final String NAME_KEY = "ut.edu.cascade.plugin.name";
    /** The resource bundle key for the description of the plugin */
    private static final String DESC_KEY = "ut.edu.cascade.plugin.description";
    /** The resource bundle key for the name of a parameter */
    //private static final String SIMPLE_PARAM_NAME_KEY = "parameter.simple.name";
    /** The resource bundle key for the description of a parameter */
    //private static final String SIMPLE_PARAM_DESC_KEY = "parameter.simple.description";

    /* (non-Javadoc)
     * @see com.cms.assetfactory.BaseAssetFactoryPlugin#doPluginActionPost(com.hannonhill.cascade.api.asset.admin.AssetFactory, com.hannonhill.cascade.api.asset.home.FolderContainedAsset)
     */
    @Override
    public void doPluginActionPost(AssetFactory factory, FolderContainedAsset asset) throws PluginException
    {
    	//code in this method will be executed after the users submits the creation.
        //This could be used for data validation or post-population/property transfer.
    	String message = "";
    	String tmpPath = "";
    	String msg = "";
    	try {
	    	Page page = (Page) asset;
			String title = page.getMetadata().getTitle();
			title = title.toLowerCase();
			//Create normalizer to remove accents
			String nfdNormalizedString = Normalizer.normalize(title, Normalizer.Form.NFD); 
		    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		    String slug = pattern.matcher(nfdNormalizedString).replaceAll("");
		    //remove strange characters
		    slug = slug.replaceAll("[,\"']", "");
		    slug = slug.replaceAll("</?\\w+>", "");
		    slug = slug.replaceAll("[^a-z0-9]", "-");
		    //remove multiple dashes that are together
		    slug = slug.replaceAll("--+",  "-");
		    //remove dashes at the start or end of the string
		    slug = slug.replaceAll("^-+", "");
		    slug = slug.replaceAll("-+$", "");
		    page.setName(slug);
		    message = "start with the folder thingy";
		    Folder currentFolder = asset.getParentFolder();
	        if (currentFolder == null)
	        {
	            currentFolder = (Folder) this.readAssetForIdentifier(asset.getParentFolderIdentifier());
	        }
		    tmpPath = currentFolder.getIdentifier().getPath().getPathAsString();
		    String[] currentPath = tmpPath.split("/");
		    Folder folder = currentFolder;
		    message = "got current folder " + asset.getPath();
		    //get start-date, which is in a unix timestamp.
		    Date startDate = page.getMetadata().getStartDate();
		    DateFormat df = new SimpleDateFormat("yyyy/MM");
		    String datePath = df.format(startDate);
		    datePath = "news/" + datePath;
		    message = "Just got entire path " + datePath;
		    //get root folder as an object
		    for (int i = 0; i < currentPath.length; i++){
		    	currentFolder = currentFolder.getParentFolder();
		    }
		    message = "got root folder " + currentFolder.getName();
		    Iterator<FolderContainedAsset> children = currentFolder.getChildren().iterator();
		    String[] datePathArr = datePath.split("/");
		    for(int i = 0; i < datePathArr.length; i++ ){
		    	String dateFolder = datePathArr[i];
		    	message = "for i: " + i;
		    	while (children.hasNext()){
			    	FolderContainedAsset child = children.next();
			    	if(child.getIdentifier().getType().equals(EntityTypes.TYPE_FOLDER) && child.getName().equals(dateFolder) ){
			    		message = "found child " + dateFolder;
			    		folder = (Folder) child;
			    		children = folder.getChildren().iterator();
			    		msg += i + ": Found " + dateFolder + " child of " + child.getParentFolder().getIdentifier().getPath().getPathAsString() + " new folder " + folder.getIdentifier().getPath().getPathAsString() + " ||| "; 
			    		break;
			    	} else {
			    		//if (child.getIdentifier().getType().equals(EntityTypes.TYPE_FOLDER)){
			    			//msg += " **folder not found: " + dateFolder + " : in " + child.getIdentifier().getPath().getPathAsString() + "**";
			    		//}
			    	}
			    }
		    }
		    if (!folder.getIdentifier().getPath().getPathAsString().equals(datePath)){
			    this.setAllowCreation(false, "Path not found: " + datePath + " . Please create path");
		    } else {
		    	page.setParentFolder(folder);
		    	this.setAllowCreation(true, "Everything Cool");
		    }
		    //this.setAllowCreation(false, message + "\n" + tmpPath + "\n" + folder.getIdentifier().getPath().getPathAsString() + "datePath: " + datePath);
		} catch (Exception e) {
			this.setAllowCreation(false, "Everything Cool");
		}
    }

    /* (non-Javadoc)
     * @see com.cms.assetfactory.BaseAssetFactoryPlugin#doPluginActionPre(com.hannonhill.cascade.api.asset.admin.AssetFactory, com.hannonhill.cascade.api.asset.home.FolderContainedAsset)
     */
    @Override
    public void doPluginActionPre(AssetFactory factory, FolderContainedAsset asset) throws PluginException
    {
        //code in this method will be executed before the user is presented with the
        //initial edit screen. This could be used for pre-population, etc.
    }

    /* (non-Javadoc)
     * @see com.cms.assetfactory.AssetFactoryPlugin#getAvailableParameterDescriptions()
     */
    public Map<String, String> getAvailableParameterDescriptions()
    {
        //build a map where the keys are the names of the parameters
        //and the values are the descriptions of the parameters
        Map<String, String> paramDescriptionMap = new HashMap<String, String>();
        //paramDescriptionMap.put(SIMPLE_PARAM_NAME_KEY, SIMPLE_PARAM_DESC_KEY);
        return paramDescriptionMap;
    }

    /* (non-Javadoc)
     * @see com.cms.assetfactory.AssetFactoryPlugin#getAvailableParameterNames()
     */
    public String[] getAvailableParameterNames()
    {
        //return a string array with all the name keys of
        //the parameters for the plugin
        //return new String[] { SIMPLE_PARAM_NAME_KEY };
    	return new String[] { };
    }

    /* (non-Javadoc)
     * @see com.cms.assetfactory.AssetFactoryPlugin#getDescription()
     */
    public String getDescription()
    {
        //return the resource bundle key of this plugin's
        //description
        return DESC_KEY;
    }

    /* (non-Javadoc)
     * @see com.cms.assetfactory.AssetFactoryPlugin#getName()
     */
    public String getName()
    {
        //return the resource bundle key of this plugin's
        //name
        return NAME_KEY;
    }
    
    /**
     * Reads and returns the actual BaseAsset proxy for the given Identifier.
     * 
     * @param id Identifier of the asset to read
     * @return BaseAsset
     * @throws PluginException
     */
    private BaseAsset readAssetForIdentifier(Identifier id) throws PluginException
    {
        BaseAsset asset = null;
        Read read = new Read();
        read.setToRead(id);
        read.setUsername(getUsername());
        try
        {
            ReadOperationResult result = (ReadOperationResult) read.perform();
            asset = result.getAsset();
        }
        catch (Exception e)
        {
            this.setAllowCreation(false, e.getMessage());
            throw new FatalPluginException(e.getMessage());
        }
        return asset;
    }
}