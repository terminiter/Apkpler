package soot.jimple.apkpler.plugin.icc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.apkpler.interf.DefaultPlugin;
import soot.jimple.apkpler.interf.SharedReferences;
import soot.jimple.apkpler.plugin.icc.instrumentation.ICCRedirectionCreator;

public class InterComponentCommunicationPlugin extends DefaultPlugin 
{
	String iccMethodsConfigPath = "";
	String iccLinksConfigPath = "";
	
	public static Set<AndroidMethod> iccMethods = null;
	Map<String, List<ICCLink>> pkg2links = null;
	
	public InterComponentCommunicationPlugin(String appPath, String name) 
	{
		super(appPath, name);
	}
	
	public InterComponentCommunicationPlugin(String appPath, String name, String input)
	{
		super(appPath, name, input);
		
		String[] paths = input.split(":");
		
		if (paths.length == 2)
		{
			this.iccMethodsConfigPath = paths[0];
			this.iccLinksConfigPath = paths[1];
		}
		else
		{
			throw new RuntimeException("Wrong config files specified [" + input + "]");
		}
	}
	
	public static boolean isICCMethod(SootMethod method)
	{
		String methodName = method.getName();
		
		if (methodName.length() < 4)
		{
			return false;
		}
		
		for (AndroidMethod am : iccMethods)
		{
			if (am.getMethodName().equals(methodName))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void sceneTransform() 
	{
		try 
		{
			ICCMethodsConfigFileParser iccMethodsParser = ICCMethodsConfigFileParser.fromFile(this.iccMethodsConfigPath);
			iccMethods = iccMethodsParser.parse();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		try
		{
			ICCLinksConfigFileParser iccLinksParser = new ICCLinksConfigFileParser(this.iccLinksConfigPath);
			pkg2links = iccLinksParser.parse();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		
		String pkgName = (String) SharedReferences.refs.get("PACKAGE-NAME");
		List<ICCLink> links = this.pkg2links.get(pkgName);
		
		//Existing at least one ICC link
		if (null != links)
		{
			for (ICCLink l : links) {
				ICCRedirectionCreator.v(pkgName).redirectToDestination(l);
			}
		}
	}
}
