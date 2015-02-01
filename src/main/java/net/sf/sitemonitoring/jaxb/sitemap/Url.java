package net.sf.sitemonitoring.jaxb.sitemap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Url {

	private String loc;

}
