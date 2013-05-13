/*
 * Created on Nov 20, 2009 by Artur Tomusiak
 * 
 * Copyright(c) 2000-2009 Hannon Hill Corporation. All rights reserved.
 */
package com.hannonhill.smt.service;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.hannonhill.smt.ChooserType;
import com.hannonhill.smt.DataDefinitionField;
import com.hannonhill.smt.ProjectInformation;
import com.hannonhill.smt.util.PathUtil;
import com.hannonhill.smt.util.XmlUtil;

/**
 * This class contains service methods for analyzing the xml file contents
 * 
 * @author Artur Tomusiak
 * @since 1.0
 */
public class XmlAnalyzer
{
    public static Set<String> FILE_TO_PAGE_EXTENSIONS;
    public static Set<String> FILE_TO_BLOCK_EXTENSIONS;

    static
    {
        FILE_TO_PAGE_EXTENSIONS = new HashSet<String>();
        FILE_TO_PAGE_EXTENSIONS.add(".html");
        FILE_TO_PAGE_EXTENSIONS.add(".php");
        FILE_TO_PAGE_EXTENSIONS.add(".jsp");
        FILE_TO_PAGE_EXTENSIONS.add(".htm");
        FILE_TO_PAGE_EXTENSIONS.add(".asp");

        FILE_TO_BLOCK_EXTENSIONS = new HashSet<String>();
        FILE_TO_BLOCK_EXTENSIONS.add(".block");
    }

    /**
     * Analyzes a folder by going through each file in the folder and subfolders using
     * {@link #analyzeFile(File, ProjectInformation)}.
     * 
     * @param folder
     * @param assetTypes
     */
    public static void analyzeFolder(File folder, ProjectInformation projectInformation)
    {
        List<File> files = FileSystem.getFolderContents(folder);
        for (File file : files)
            analyzeFile(file, projectInformation);
    }

    /**
     * Analyzes the data definition xml and returns a map of text fields and file chooser fields
     * 
     * @param xml
     * @return
     * @throws Exception
     */
    public static Map<String, DataDefinitionField> analyzeDataDefinitionXml(String xml) throws Exception
    {
        Map<String, DataDefinitionField> returnMap = new HashMap<String, DataDefinitionField>();
        Node rootNode = XmlUtil.convertXmlToNodeStructure(new InputSource(new StringReader(xml)));
        NodeList children = rootNode.getChildNodes();
        analyzeDataDefinitionGroup(children, "", "", returnMap);
        return returnMap;
    }

    /**
     * Returns the value of the first "src" attribute found in given xml
     * 
     * @param xml
     * @return
     * @throws Exception
     */
    public static String getFirstSrcAttribute(String xml) throws Exception
    {
        // Add root tag to make it a valid xml
        String xmlWithRoot = XmlUtil.addRootTag(xml);
        Node rootNode = XmlUtil.convertXmlToNodeStructure(new InputSource(new StringReader(xmlWithRoot)));
        return getFirstSrcAttribute(rootNode);
    }

    /**
     * Checks if current node contains an src attribute and if not, then recursively checks all the ancestor
     * nodes and returns
     * the values first one that contains.
     * 
     * @param node
     * @return
     */
    private static String getFirstSrcAttribute(Node node)
    {
        if (node.getAttributes() != null && node.getAttributes().getNamedItem("src") != null)
            return node.getAttributes().getNamedItem("src").getTextContent();

        if (node.getChildNodes() != null)
            for (int i = 0; i < node.getChildNodes().getLength(); i++)
            {
                String src = getFirstSrcAttribute(node.getChildNodes().item(i));
                if (src != null)
                    return src;
            }

        return null;
    }

    /**
     * Adds all the text fields to the returnMap recursively
     * 
     * @param children
     * @param identifierPrefix
     * @param labelPrefix
     * @param returnMap
     */
    private static void analyzeDataDefinitionGroup(NodeList children, String identifierPrefix, String labelPrefix,
            Map<String, DataDefinitionField> returnMap)
    {
        for (int i = 0; i < children.getLength(); i++)
        {
            Node node = children.item(i);

            // if node has no attributes, it must be a text node or comment node - ignore these
            if (node.getAttributes() == null)
                continue;

            // figure out the identifier
            String identifier = "";
            Node identifierNode = node.getAttributes().getNamedItem("identifier");
            if (identifierNode != null)
                identifier = identifierNode.getTextContent();

            // figure out the identifier - or use label if identifier doesn't exist
            String label = "";
            Node labelNode = node.getAttributes().getNamedItem("label");
            if (labelNode != null)
                label = labelNode.getTextContent();
            else
                label = identifier;

            String newIdentifier = identifierPrefix + identifier;
            String newLabel = labelPrefix + label;

            // If group - go recursively, if text - add to the field list. Ignore asset choosers.
            if (node.getNodeName().equals("group"))
                analyzeDataDefinitionGroup(node.getChildNodes(), identifierPrefix + identifier + "/", labelPrefix + label + "/", returnMap);
            else if (node.getNodeName().equals("text"))
                returnMap.put(newIdentifier, new DataDefinitionField(newIdentifier, newLabel, null, isMultiple(node)));
            else if (node.getNodeName().equals("asset") && node.getAttributes().getNamedItem("type") != null
                    && node.getAttributes().getNamedItem("type").getTextContent().equals("file"))
                returnMap.put(newIdentifier, new DataDefinitionField(newIdentifier, newLabel, ChooserType.FILE, isMultiple(node)));
            else if (node.getNodeName().equals("asset") && node.getAttributes().getNamedItem("type") != null
                    && node.getAttributes().getNamedItem("type").getTextContent().equals("block"))
                returnMap.put(newIdentifier, new DataDefinitionField(newIdentifier, newLabel, ChooserType.BLOCK, isMultiple(node)));
        }
    }

    /**
     * Returns true if provided node has a multiple="true" attribute
     * 
     * @param node
     * @return
     */
    private static boolean isMultiple(Node node)
    {
        return node.getAttributes().getNamedItem("multiple") != null && node.getAttributes().getNamedItem("multiple").getTextContent().equals("true");
    }

    /**
     * Adds the file to the list of files to process and collects the extension
     * 
     * @param file
     * @param projectInformation
     */
    private static void analyzeFile(File file, ProjectInformation projectInformation)
    {
        // Skip hidden files and folders
        if (file.getName().startsWith("."))
            return;

        // Recursively analyze sub-folders
        if (file.isDirectory())
        {
            analyzeFolder(file, projectInformation);
            return;
        }

        String fileNameWihtoutXmlExtension = PathUtil.truncateExtension(file.getName());
        String extension = PathUtil.getExtension(fileNameWihtoutXmlExtension);
        projectInformation.getGatheredExtensions().add(extension);
        projectInformation.getFilesToProcess().add(file);
    }

}
