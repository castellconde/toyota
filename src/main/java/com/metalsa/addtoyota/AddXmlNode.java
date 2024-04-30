/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.metalsa.addtoyota;


import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
/**
 *
 * @author castellconde
 */
public class AddXmlNode {

    
    public static void main(String[] args) throws Exception {
        
        if(args.length != 2)
            System.exit(0);
        
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(args[0]);
        Element root = document.getDocumentElement();

        
        NodeList oldAddenda = document.getElementsByTagName("cfdi:Addenda");
        
        if(oldAddenda.getLength()>0) {
            System.out.println("Ya existe Addenda:"+oldAddenda.item(0).getNodeName());
            root.removeChild(oldAddenda.item(0));
        }
        
        String cfdiTotal = document.getElementsByTagName("cfdi:Comprobante").item(0).getAttributes().getNamedItem("Total").getTextContent();
        String NoIdentificacion = document.getElementsByTagName("cfdi:Concepto").item(0).getAttributes().getNamedItem("NoIdentificacion").getTextContent();
        
        Collection<Addenda> servers = new ArrayList<Addenda>();
        servers.add(new Addenda());

        for (Addenda server : servers) {
            // server elements
            Element newAddenda = document.createElement("cfdi:Addenda");
            Element addToyota = document.createElement("AddendaToyota");
            addToyota.setAttribute("xmlns", "http://www.pegasotecnologia.com/secfd/schemas/AddendaReceptorToyota");
            Element tmmgt = document.createElement("TMMGT");
            Element shipmentOrder = document.createElement("shipmentOrder");
            shipmentOrder.setAttribute("shipmentId", args[1]+"#"+NoIdentificacion);
            shipmentOrder.setAttribute("Amount", cfdiTotal); 

            tmmgt.appendChild(shipmentOrder);
            addToyota.appendChild(tmmgt);
            newAddenda.appendChild(addToyota);
            root.appendChild(newAddenda);
        }
        
        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(args[0]);
        transformer.transform(source, result);
    }

    public static class Addenda {
//        public String getAddendaToyota() { return "<TMMGT><shipmentOrder shipMentId=\"12271233#YN40005443\" Amount=\"81773.04\"></TMMGT></TMMGT>"; }
    }
}
