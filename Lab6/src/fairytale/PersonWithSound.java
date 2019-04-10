package fairytale;
//Человек-заготовка для работы со звуком
public abstract class PersonWithSound extends PersonWithHouse {

    private String currentSound;//Звук, с которым работает человек

    public PersonWithSound(String name,int pictureNumber,int comfort){
        super(name,pictureNumber,comfort);
        currentSound="";
    }
    @Override
    public int hashCode() {
        return toString().hashCode()+currentSound.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof PersonWithSound) return false;
        PersonWithSound other = (PersonWithSound) obj;
        return getName().equals(other.getName()) &&
                currentSound.equals(other.currentSound);
    }

    @Override
    public void describe() {
        System.out.println(getName()+" могу работать со звуком.");
    }

    public String getCurrentSound() {
        return currentSound;
    }
    public void setCurrentSound(String currentSound) {
        this.currentSound = currentSound;
    }
}