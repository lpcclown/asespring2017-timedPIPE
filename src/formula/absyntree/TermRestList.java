package formula.absyntree;
import java.util.*;
import java.util.function.Consumer;

public class TermRestList extends ArrayList<TermRest> {
	public Vector<TermRest> list;

	public TermRestList(){
		list = new Vector<>();
	}

	public void addElement(TermRest tr){
		list.addElement(tr);
	}

	public TermRest elementAt(int i){
		return list.elementAt(i);
	}

	public int size() {
      return list.size();
    }

	@Override
	public Iterator<TermRest> iterator() {
		return list.iterator();
	}

	@Override
	public void forEach(Consumer<? super TermRest> action) {
		list.forEach(action);
	}

	@Override
	public Spliterator<TermRest> spliterator() {
		return list.spliterator();
	}
}
