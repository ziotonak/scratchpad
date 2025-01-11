
import java.lang.reflect.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

sealed interface JSONValue
		permits JSONArray, JSONObject, JSONPrimitive {

	public default String type() {
		if (this instanceof JSONArray)
			return "array";
		else if (this instanceof JSONObject)
			return "object";
		else if (this instanceof JSONNumber)
			return "number";
		else if (this instanceof JSONString)
			return "string";
		else if (this instanceof JSONBoolean)
			return "boolean";
		else
			return "null";
	}

}

final class JSONArray extends ArrayList<JSONValue> implements JSONValue {
}

final class JSONObject extends HashMap<String, JSONValue> implements JSONValue {

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("{");
		for (Map.Entry<String, JSONValue> entry : entrySet()) {
			if (result.length() > 1)
				result.append(",");
			result.append(" \"");
			result.append(entry.getKey());
			result.append("\": ");
			result.append(entry.getValue());
		}
		result.append(" }");
		return result.toString();
	}

}

sealed interface JSONPrimitive extends JSONValue
		permits JSONNumber, JSONString, JSONBoolean, JSONNull {
}

final record JSONNumber(double value) implements JSONPrimitive {

	@Override
	public String toString() {
		return "" + value;
	}

}

final record JSONString(String value) implements JSONPrimitive {

	@Override
	public String toString() {
		return "\"" + value.translateEscapes() + "\"";
	}

}

enum JSONBoolean implements JSONPrimitive {
	FALSE, TRUE;

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}

}

enum JSONNull implements JSONPrimitive {
	INSTANCE;

	@Override
	public String toString() {
		return "null";
	}

}

public class Main {

	public enum Size {
		SMALL("S"), MEDIUM("M"), LARGE("L"), EXTRA_LARGE("XL");

		private final String label;

		Size(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

	};

	public static class Person {

		private final String name;

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getClass().getName() + "[name=" + name + "]";
		}

		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject)
				return true;
			if (otherObject == null)
				return false;
			if (getClass() != otherObject.getClass())
				return false;

			var other = (Person) otherObject;

			return Objects.equals(name, other.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name);
		}

	}

	public static class Student extends Person {

		private final String major;

		public Student(String name, String major) {
			super(name);
			this.major = major;
		}

		public String getMajor() {
			return major;
		}

		@Override
		public String toString() {
			return super.toString() + "[major=" + major + "]";
		}

		@Override
		public boolean equals(Object otherObject) {
			if (!super.equals(otherObject))
				return false;

			var other = (Student) otherObject;

			return Objects.equals(major, other.major);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), major);
		}

	}

	/**
	 * This method grows an array by allocating a new array of the same type and
	 * copying all elements.
	 *
	 * @param a         the array to grow. This can be an Object array or
	 *                  primitive type array
	 * @param newLength the length of the new array
	 * @return a larger array that contains all elements of a
	 */
	static Object copyOf(Object a, int newLength) {
		Class<?> cl = a.getClass();
		if (!cl.isArray())
			return null;
		Class<?> componentType = cl.getComponentType();
		int length = Array.getLength(a);
		Object newArray = Array.newInstance(componentType, newLength);
		System.arraycopy(a, 0, newArray, 0, Math.min(length, newLength));
		return newArray;
	}

	public static void main(String[] args) throws ReflectiveOperationException {
		int[] a = { 1, 3, 5, 7, 11 };
		a = (int[]) copyOf(a, 9);
		System.out.println(Arrays.toString(a));
		System.out.println(a.getClass().arrayType());
		System.out.println(a.getClass().componentType());

		var harry = new Person("Harry");
		Class<?> cl = harry.getClass();

		Method m = cl.getMethod("getName");

		// for static methods the first parameter is ignored (can be set to null)
		// Object invoke(implicit parameter, explicit... parameters) all Objects
		// primitives are returned in wrappers
		// e.g. double d = (Double) m.invoke(...)
		var name = (String) m.invoke(harry);
		System.out.println(name);

		Field f = cl.getDeclaredField("name");
		f.setAccessible(true); // encapsulation broken
		f.set(harry, "hello");
		Object v = f.get(harry);
		assert v instanceof String;
		System.out.println(v);

		Person person = new Person("Harry");
		System.out.println(person);

		Student student1 = new Student("Alice", "Chemistry");
		Student student2 = new Student("Alice", "Chemistry");
		Student student3 = new Student("Bob", "Chemistry");
		System.out.println(student1);

		// pass the -ea flag to the JVM to enable assertions
		assert student1.hashCode() == student2.hashCode() : "Message";
		assert student1.equals(student2);
		assert student1.hashCode() != student3.hashCode();
		assert !student1.equals(student3);

		System.out.println(person.hashCode());
		System.out.println(student1.hashCode());
		System.out.println(student2.hashCode());
		System.out.println(student3.hashCode());

		System.out.println(Arrays.toString(Person.class.getFields()));
		System.out.println(Arrays.toString(Person.class.getDeclaredFields()));

		System.out.println(Arrays.toString(Student.class.getFields()));
		System.out.println(Arrays.toString(Student.class.getDeclaredFields()));

		String className = "java.util.Random";
		try {
			System.out.println(Class.forName(className).getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		JSONObject obj = new JSONObject();
		obj.put("name", new JSONString("Harry"));
		obj.put("salary", new JSONNumber(50000));
		obj.put("married", JSONBoolean.FALSE);

		JSONArray arr = new JSONArray();
		arr.add(new JSONNumber(13));
		arr.add(new JSONNumber(21));
		arr.add(JSONNull.INSTANCE);
		arr.add(new JSONNumber(17));

		obj.put("luckyNumbers", arr);

		System.out.println(obj);
		System.out.println(obj.type());

		var size = Size.MEDIUM;
		System.out.println(size);

		System.out.println(size.getLabel());

		System.out.println(Size.valueOf("SMALL").getLabel());
		System.out.println(Enum.valueOf(Size.class, "SMALL").getLabel());

		Size[] values = Size.values();
		System.out.println(values);
		System.out.println(Arrays.toString(values));

		String label = switch (size) {
			case Size.SMALL -> "S";
			// no need for 'Size.' deducted from expression type
			case MEDIUM -> "M";
			case LARGE -> "L";
			case EXTRA_LARGE -> "XL";
			// case SMALL, MEDIUM, LARGE -> ":3";
			// default -> "???";
		};
		System.out.println(label);

		var builder = new StringBuilder();
		builder.append("\uD83D\uDC7D");
		builder.appendCodePoint(0x1F47D);
		var string = builder.toString();
		System.out.println(string);

		var textBlock = """
				<div class="Warning">
				    Beware of those who say "Hello" to the world
				</div>
				""";
		System.out.println(textBlock);
	}

}
