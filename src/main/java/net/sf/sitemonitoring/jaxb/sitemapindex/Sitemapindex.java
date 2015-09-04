package net.sf.sitemonitoring.jaxb.sitemapindex;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Sitemapindex {

	@XmlElement(name = "sitemap")
	private List<Sitemap> sitemaps;

}
