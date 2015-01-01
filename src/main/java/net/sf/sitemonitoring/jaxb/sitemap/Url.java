package net.sf.sitemonitoring.jaxb.sitemap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Url {

	@XmlElement(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
	private String loc;

}
