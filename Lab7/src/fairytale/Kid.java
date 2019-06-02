package fairytale;
//Plays sound after listening
public class Kid extends PersonWithSound implements SoundListener{

    public static class HouseComparator{
        private static House karlsonHouse;

        public static House getKarlsonHouse() {
            return karlsonHouse;
        }
        public static void setKarlsonHouse(House karlsonHouse) {
            HouseComparator.karlsonHouse = karlsonHouse;
        }

        public static void compareToKarlsonHouse(House house)throws KarlsonHouseNotTheBestException {
            int houseRaise=karlsonHouse.getPicturesCount()*karlsonHouse.getComfort()-
                    (house.getPicturesCount()*house.getComfort());
            if (houseRaise>0) {
                System.out.println("Дом Карлсона лучше, чем "+house);
            }else throw new KarlsonHouseNotTheBestException();
            
        }
    }
    private int concentration;//Внимательность слушанья (в процентах)

    public Kid(String name,int pictureCount,int comfort) {
        super(name,pictureCount,comfort);
        concentration=75;
    }
    private void checkSound()throws VeryLongSoundException {
        if(getCurrentSound().length()> VeryLongSoundException.CRITICAL_LENGTH)throw new VeryLongSoundException();
    }
    @Override
    public void playSound() {
        boolean soundWasComfortable=true;
        try {
            checkSound();
        }catch (VeryLongSoundException e){
            soundWasComfortable=false;
            System.out.println(e);
            setCurrentSound(getCurrentSound().substring(0, VeryLongSoundException.CRITICAL_LENGTH));
        }
        System.out.printf("%s услышал: %s\n",getName(),getCurrentSound());
        if(soundWasComfortable) System.out.println(getName()+" подумал: \"Мне так хорошо.\"");
        else System.out.println(getName()+" подумал: \"Я устал слушать.\"");
    }

    @Override
    public String listen(String sound, Iterable<? extends SoundChanger> soundChanger) {
        setCurrentSound(sound);
        for(SoundChanger sc:soundChanger){
            if (Math.random()*100>=concentration)
                setCurrentSound(sc.changeSound(getCurrentSound()));
        }
        return getCurrentSound();
    }
    public int getConcentration() {
        return concentration;
    }
    public void setConcentration(int concentration) {
        this.concentration=concentration;
        if(concentration<0)this.concentration = 0;
        if(concentration>100)this.concentration=100;
    }
    @Override
    public int hashCode() {
        return super.hashCode()*concentration;
    }
    @Override
    public boolean equals(Object o) {
        if(!(super.equals(o)&&(o instanceof Kid)))return false;
        Kid other=(Kid)o;
        return concentration==other.concentration;
    }
}