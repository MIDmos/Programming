package fairytale;

public abstract class PersonWithHouse implements Describable {
    private final String NAME;
    private House house;
    public class House{
        private int pictureCount;
        private int comfort;
        public House(int picturesNumber, int comfort){
            this.pictureCount=picturesNumber;
            this.comfort=comfort;
        }

        public int getPicturesCount() {
            return pictureCount;
        }

        public void setPicturesNumber(int picturesNumber) {
            if(picturesNumber>=0)
                this.pictureCount = picturesNumber;
        }

        public int getComfort() {
            return comfort;
        }

        public void setComfort(int comfort) {
            this.comfort = comfort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof House)) return false;
            House house = (House) o;
            return pictureCount == house.pictureCount &&
                    comfort == house.comfort;
        }

        @Override
        public int hashCode() {
            return pictureCount*comfort*194873;
        }

        @Override
        public String toString() {
            return "Дом, в котором живет "+NAME+" с "+pictureCount+" картинами и уровнем комфорта "+comfort;
        }
    }
    public PersonWithHouse(String name,int pictureNumber,int comfort){
        NAME=name;
        house=new House(pictureNumber,comfort);
    }

    public String getName() {
        return NAME;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("Я %s с домом (%d, %d)",NAME,house.pictureCount,house.comfort);
    }
}
