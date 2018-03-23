
public class DataEntry {
	String sourceName;
	String author;
	String title;
	String url;
	String publishedAt;
	
	public String getCSVSourceName() {
		return toCSVFriendly(this.sourceName);
	}
	
	public String getCSVAuthor() {
		return toCSVFriendly(this.author);
	}
	
	public String getCSVTitle() {
		return toCSVFriendly(this.title);
	}

	public String getCSVUrl() {
		return toCSVFriendly(this.url);
	}
	
	public String getCSVPublishedAt() {
		return toCSVFriendly(this.publishedAt);
	}
	
	public String toCSVFriendly(String in) {
		return "\""+in+"\"";
	}

	@Override
	public boolean equals(Object o) {
		
		if (o == null || o.getClass() != this.getClass()) return false;
		
		if (((DataEntry)o).url.equals(this.url) && ((DataEntry)o).title.equals(this.title)) return true;

		return false;
	}
}

