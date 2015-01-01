package net.sf.sitemonitoring.jaxb.sitemap;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

// TODO Is it possible to have namespaces nicer? I googled this (to package.info):

//@XmlSchema(
//        namespace="http://www.example.com/customer",
//        elementFormDefault=XmlNsForm.QUALIFIED)

@Data
@XmlRootElement(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
@XmlAccessorType(XmlAccessType.FIELD)
public class Urlset {

	@XmlElement(name = "url", namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
	private List<Url> urls;

}
