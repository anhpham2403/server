package com.anh.currency;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import com.anh.currency.model.Feed;

public class RSSFeedParser {
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String ITEM = "item";
	static final String PUB_DATE = "pubDate";
	final URL url;

	public RSSFeedParser(String feedUrl) {
		try {
			this.url = new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Feed> readFeed() {
		List<Feed> listFeeds = new ArrayList<>();
		try {
			String title = "";
			String pubdate = "";
			String description = "";
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			InputStream in = read();
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
			boolean isItem = false;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();
				if (event.isStartElement()) {
					String localPart = event.asStartElement().getName().getLocalPart();
					switch (localPart) {
					case ITEM:
						isItem = true;
						break;
					case TITLE:
						if (isItem) {
							title = getCharacterData(event, eventReader);
							String[] wk_str = title.split("\\(");
							if(wk_str.length>1) {
								title = wk_str[wk_str.length-1].split("\\)")[0];
							} else {
								title = wk_str[0].split("\\)")[0];
							}
						} 
						break;
					case DESCRIPTION:
						if (isItem) {
							description = getCharacterData(event, eventReader);
							String[] wk_str1 = description.split("=");
							if(wk_str1.length>1) {
								description = wk_str1[1].split(" ")[1];
							} else {
								description = wk_str1[0].split(" ")[1];
							}	
						} 
						break;
					case PUB_DATE:
						if (isItem) {
							pubdate = getCharacterData(event, eventReader);
						}
						break;
					}
				} else if (event.isEndElement()) {
					if (event.asEndElement().getName().getLocalPart() == (ITEM)) {
						Feed feed = new Feed();
						feed.setDescription(description); 
						feed.setTitle(title);
						feed.setPubDate(pubdate);
						listFeeds.add(feed);
						isItem = false;
						continue;
					}
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return listFeeds;
	}

	private String getCharacterData(XMLEvent event, XMLEventReader eventReader) throws XMLStreamException {
		String result = "";
		event = eventReader.nextEvent();
		if (event instanceof Characters) {
			result = event.asCharacters().getData();
		}
		return result;
	}

	private InputStream read() {
		try {
			return url.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}