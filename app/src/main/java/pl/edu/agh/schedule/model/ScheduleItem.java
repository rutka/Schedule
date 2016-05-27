package pl.edu.agh.schedule.model;

public class ScheduleItem implements Cloneable, Comparable<ScheduleItem> {

    // start and end time for this item
    public long startTime = 0;
    public long endTime = 0;

    // title and description
    public String title = "";
    public String description = "";

    // background image URL
    public String backgroundImageUrl = ""; // FIXME need to hardcode img

    @Override
    public Object clone()  {
        try {
            return super.clone();
        } catch (CloneNotSupportedException unused) {
            // does not happen (since we implement Cloneable)
            return new ScheduleItem();
        }
    }

    @Override
    public int compareTo(ScheduleItem another) {
        return this.startTime < another.startTime ? -1 :
                ( this.startTime > another.startTime ? 1 : 0 );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ScheduleItem)) {
            return false;
        }
        ScheduleItem i = (ScheduleItem) o;
        return title.equals(i.title) && startTime == i.startTime &&
                endTime == i.endTime;
    }

    @Override
    public String toString() {
        return String.format("[startTime=%d, endTime=%d, title=%s, description=%s]",
                startTime, endTime, title, description);
    }
}
