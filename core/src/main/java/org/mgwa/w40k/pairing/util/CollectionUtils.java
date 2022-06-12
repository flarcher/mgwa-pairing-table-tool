package org.mgwa.w40k.pairing.util;

import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class CollectionUtils {
	private CollectionUtils() {}

	public static <T> T getOneAndOnly(Stream<T> stream) {
		return getOneAndOnly(stream::iterator);
	}

	public static <T> T getOneAndOnly(Iterable<T> iterable) {
		Iterator<T> iterator = iterable.iterator();
		if (!iterator.hasNext()) {
			throw new IllegalStateException("0 item");
		}
		T item = iterator.next();
		if (iterator.hasNext()) {
			throw new IllegalStateException("Several items");
		}
		return item;
	}

}
