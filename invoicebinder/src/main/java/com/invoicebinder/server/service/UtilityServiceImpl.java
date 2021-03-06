/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.invoicebinder.server.service;

import static com.invoicebinder.invoicebindercore.exception.ExceptionManager.getFormattedExceptionMessage;
import com.invoicebinder.invoicebindercore.exception.ExceptionType;
import com.invoicebinder.invoicebindercore.shell.ShellExecuteResult;
import com.invoicebinder.client.service.utility.UtilityService;
import com.invoicebinder.server.dataaccess.AutoNumDAO;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.invoicebinder.server.logger.ServerLogManager;
import com.invoicebinder.server.serversettings.ServerSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.invoicebinder.invoicebindercore.file.FileManager;
import com.invoicebinder.invoicebindercore.pdf.PDFManager;

import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author mon2
 */
@SuppressWarnings("serial")
@Transactional(rollbackFor = RuntimeException.class)
@Service("utility")
public class UtilityServiceImpl extends RemoteServiceServlet implements UtilityService {
    @Autowired
    private AutoNumDAO autonumDAO;
    
    @Override
    public String getNextAutoNum(String autoNumId) {
        return autonumDAO.getAutoNum(autoNumId);
    }
    
    @Override
    public String createPDFFile(String contentHtml, String invoiceNumber) {
        String path = "", htmlFilePath, pdfFilePath, wkhtmltopdflocation;
        ShellExecuteResult result;
        String pdfFileName = invoiceNumber + ".pdf";
        String tempFileName = UUID.randomUUID().toString() + ".html";
        String OS = System.getProperty("os.name").toLowerCase();
        ServerLogManager.writeInformationLog(UtilityServiceImpl.class, "Creating PDF file");
        
        try {
            wkhtmltopdflocation = ServerSettingsManager.ApplicationSettings.getWKHTMLtoPDFLocation();
            path = ServerSettingsManager.ApplicationSettings.getUploadPath();
            ServerLogManager.writeInformationLog(UtilityServiceImpl.class, "Upload path: " + path);
            if (FileManager.writeFile(path + tempFileName, contentHtml)) {
                ServerLogManager.writeDebugLog(UtilityServiceImpl.class, String.format("Creating temp file: %s%s", path, tempFileName));
                ServerLogManager.writeDebugLog(UtilityServiceImpl.class, "wkhtmltopdf location: " + wkhtmltopdflocation);

                ServerLogManager.writeInformationLog(UtilityServiceImpl.class, "Creating PDF file");

                //handle spaces in path for windows
                if ((OS.contains("win"))) {
                    //enclose path in quotes to deal with spaces
                    htmlFilePath = String.format("\"%s%s\"", path,tempFileName);
                    pdfFilePath = String.format("\"%s%s\"", path,pdfFileName);
                }
                else {
                    htmlFilePath = String.format("%s%s", path,tempFileName);
                    pdfFilePath = String.format("%s%s", path,pdfFileName);
                }

                ServerLogManager.writeDebugLog(UtilityServiceImpl.class, htmlFilePath);
                ServerLogManager.writeDebugLog(UtilityServiceImpl.class, pdfFilePath);

                result = PDFManager.convertHTMLToPDF(wkhtmltopdflocation, htmlFilePath,pdfFilePath);
                if (!result.isSuccess()){
                    throw new IOException(result.getErrorOutput());
                }
                ServerLogManager.writeInformationLog(UtilityServiceImpl.class, "PDF File saved: " + path + pdfFileName);
                FileManager.deleteFile(path + tempFileName);
            }
        }   catch (IOException | InterruptedException ex) {
            pdfFileName = "";
            ServerLogManager.writeErrorLog(UtilityServiceImpl.class, getFormattedExceptionMessage(ExceptionType.ServiceException, ex));
        }
        ServerLogManager.writeInformationLog(UtilityServiceImpl.class, "End creating PDF file");
        return path + pdfFileName;
    }
}
