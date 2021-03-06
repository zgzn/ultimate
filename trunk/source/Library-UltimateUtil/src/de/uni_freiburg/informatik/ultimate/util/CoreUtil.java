/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 *
 * This file is part of the ULTIMATE Core.
 *
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Core grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 *
 * @author dietsch@informatik.uni-freiburg.de
 *
 */
public class CoreUtil {

	private static final String PLATFORM_LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String OS = System.getProperty("os.name");
	public static final boolean OS_IS_WINDOWS = OS.toLowerCase().indexOf("win") >= 0;

	public static String getPlatformLineSeparator() {
		return PLATFORM_LINE_SEPARATOR;
	}

	public static String getIsoUtcTimestamp() {
		final TimeZone tz = TimeZone.getTimeZone("UTC");
		// Quoted "Z" to indicate UTC, no timezone offset
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		return df.format(new Date());
	}

	/**
	 * Traverses the OS' PATH and searches for a file that fulfills the following conditions.
	 * <ul>
	 * <li>It is named <name>,
	 * <li>the current process is allowed to execute it,
	 * <li>it looks like some known executable binary.
	 * </ul>
	 */
	public static File findExecutableBinaryOnPath(final String name) {
		final Predicate<File> funLooksLikeExectuable;
		if (CoreUtil.OS_IS_WINDOWS) {
			// Check for Windows executable:
			// Windows uses the Portable Executable format, which should always start with the magic number 4d5a
			// (ASCII characters MZ)
			funLooksLikeExectuable = f -> {
				final byte[] firstBytes = new byte[4];
				try {
					final FileInputStream input = new FileInputStream(f);
					input.read(firstBytes);
					input.close();

					if (firstBytes[0] == 0x4d && firstBytes[1] == 0x5a) {
						return true;
					}
					return false;
				} catch (final Exception e) {
					return false;
				}
			};
		} else {
			// just assume linux: ELF format executable used by Linux start with 7f454c46
			funLooksLikeExectuable = f -> {
				final byte[] firstBytes = new byte[8];
				try {
					final FileInputStream input = new FileInputStream(f);
					input.read(firstBytes);
					input.close();
					if (firstBytes[0] == 0x7f && firstBytes[1] == 0x45 && firstBytes[1] == 0x4c
							&& firstBytes[1] == 0x46) {
						return true;
					}
					return false;
				} catch (final Exception e) {
					return false;
				}
			};
		}

		for (final String dirname : System.getenv("PATH").split(File.pathSeparator)) {
			final File[] files = new File(dirname).listFiles(f -> f.getName().startsWith(name));
			if (files == null) {
				continue;
			}
			for (final File file : files) {
				if (file.isFile() && file.canExecute() && funLooksLikeExectuable.test(file)) {
					return file;
				}
			}
		}
		return null;
	}

	public static File writeFile(final String filename, final String content) throws IOException {
		return writeFile(filename, content, false);
	}

	public static File writeFile(final String filename, final String[] content) throws IOException {
		return writeFile(filename, content, false);
	}

	public static File appendFile(final String filename, final String content) throws IOException {
		return writeFile(filename, content, true);
	}

	public static File appendFile(final String filename, final String[] content) throws IOException {
		return writeFile(filename, content, true);
	}

	private static File writeFile(final String filename, final String[] content, final boolean append)
			throws IOException {
		if (content == null || content.length == 0) {
			return null;
		}
		final File file = createFile(filename);
		final IWriterConsumer funWrite = fw -> {
			for (final String line : content) {
				fw.append(line);
				fw.append(PLATFORM_LINE_SEPARATOR);
			}
		};
		writeFile(funWrite, append, file);
		return file;
	}

	private static File writeFile(final String filename, final String content, final boolean append)
			throws IOException {
		if (content == null || content.isEmpty()) {
			return null;
		}
		final File file = createFile(filename);
		writeFile(fw -> fw.append(content), append, file);
		return file;
	}

	private static void writeFile(final IWriterConsumer funWrite, final boolean append, final File file)
			throws IOException {
		try (FileOutputStream os = new FileOutputStream(file, append);
				Writer fw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
			funWrite.consume(fw);
			fw.close();
		}
	}

	private static File createFile(final String filename) {
		final File file = new File(filename);
		if (!file.isDirectory()) {
			final File parentFile = file.getParentFile();
			if (parentFile != null) {
				parentFile.mkdirs();
			}
		}
		return file;
	}

	public static List<String> readFileLineByLine(final String filename) throws IOException {
		final BufferedReader br =
				new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF8"));
		final List<String> rtr = new ArrayList<>();
		try {
			String line = br.readLine();
			while (line != null) {
				rtr.add(line);
				line = br.readLine();
			}
			return rtr;
		} finally {
			br.close();
		}
	}

	public static String readFile(final String filename) throws IOException {
		final StringBuilder sb = new StringBuilder();
		readFileLineByLine(filename).stream().forEach(line -> sb.append(line).append(PLATFORM_LINE_SEPARATOR));
		return sb.toString();
	}

	public static String readFile(final File file) throws IOException {
		return readFile(file.getAbsolutePath());
	}

	public static List<String> readFileLineByLine(final File file) throws IOException {
		return readFileLineByLine(file.getAbsolutePath());
	}

	/**
	 * Get the extension of a file, i.e., the part of the filename after the last '.'. If there is no extension, return
	 * an empty string.
	 *
	 * @param file
	 *            The file for which the extension should be obtained.
	 * @return The extension.
	 */
	public static String getFileExtension(final File file) {
		assert file != null;
		assert file.isFile();
		assert !file.isDirectory();
		final String filename = file.getName();

		final int i = filename.lastIndexOf('.');
		if (i > 0) {
			return filename.substring(i + 1);
		}
		return "";
	}

	/**
	 * Returns all elements of a collection that match the check defined by predicate.
	 *
	 * @param collection
	 *            The collection you want to filter. May not be null.
	 * @param predicate
	 *            The predicate you want to use to filter said collection. May not be null.
	 * @return A new collection that only contains elements for which {@link IPredicate#check(Object)} returned true.
	 */
	public static <E> Collection<E> where(final Collection<E> collection, final Predicate<E> predicate) {
		final ArrayList<E> rtr = new ArrayList<>();
		for (final E entry : collection) {
			if (predicate.test(entry)) {
				rtr.add(entry);
			}
		}
		return rtr;
	}

	/**
	 * Returns a {@link Set} of elements that are created by applying the reducer to every element in the collection.
	 *
	 * @param collection
	 *            May not be null.
	 * @param reducer
	 *            May not be null.
	 * @return
	 */
	public static <T, E> Set<T> selectDistinct(final Collection<E> collection, final IReduce<T, E> reducer) {
		final Set<T> rtr = new HashSet<>();
		for (final E entry : collection) {
			rtr.add(reducer.reduce(entry));
		}
		return rtr;
	}

	public static <T, E> Collection<T> select(final Collection<E> collection, final IReduce<T, E> reducer) {
		final Collection<T> rtr = new ArrayList<>();
		for (final E entry : collection) {
			rtr.add(reducer.reduce(entry));
		}
		return rtr;
	}

	public static <E> Collection<E> flattenMapValuesToCollection(final Map<?, E> map) {
		final Collection<E> rtr = new ArrayList<>();
		for (final Entry<?, E> entry : map.entrySet()) {
			rtr.add(entry.getValue());
		}
		return rtr;
	}

	public static <T, E> T reduce(final Set<E> collection, final IMapReduce<T, E> reducer) {
		T lastValue = null;
		for (final E entry : collection) {
			lastValue = reducer.reduce(lastValue, entry);
		}
		return lastValue;
	}

	public static <T, E> T reduce(final Collection<E> collection, final IMapReduce<T, E> reducer) {
		T lastValue = null;
		for (final E entry : collection) {
			lastValue = reducer.reduce(lastValue, entry);
		}
		return lastValue;
	}

	/**
	 * Indents a (possibly multiline) String such that the resulting StringBuilder object contains the same String, but
	 * indented with the indentPrefix. It also converts line breaks to the system-specific line separator.
	 *
	 * @param original
	 * @param indentPrefix
	 * @param forceRemoveLastLinebreak
	 *            When true, the last linebreak will always be removed, when false, an existing last line break will be
	 *            preserved (but converted to system-specific line break)
	 * @return
	 */
	public static StringBuilder indentMultilineString(final String original, final String indentPrefix,
			final boolean forceRemoveLastLinebreak) {
		final StringBuilder sb = new StringBuilder();
		final String lineSeparator = System.getProperty("line.separator");
		final String[] splitted = original.split("\\r?\\n");

		for (final String s : splitted) {
			sb.append(indentPrefix).append(s).append(lineSeparator);
		}

		final char last = original.charAt(original.length() - 1);
		if (forceRemoveLastLinebreak || (last != '\n' && last != '\r')) {
			sb.replace(sb.length() - lineSeparator.length(), sb.length(), "");
		}
		return sb;
	}

	public static String getCurrentDateTimeAsString() {
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(Calendar.getInstance().getTime());
	}

	/**
	 * Flattens a string, i.e. removes all line breaks and replaces them with separator
	 *
	 * @param original
	 * @param separator
	 * @return
	 */
	public static StringBuilder flatten(final String original, final String separator) {
		final StringBuilder sb = new StringBuilder();
		final String[] splitted = original.split("\\r?\\n");
		for (final String s : splitted) {
			sb.append(s).append(separator);
		}
		sb.replace(sb.length() - separator.length(), sb.length(), "");
		return sb;
	}

	public static <E> Collection<E> firstN(final Collection<E> collection, final int n) {
		final ArrayList<E> rtr = new ArrayList<>(n);
		int i = 1;
		for (final E elem : collection) {
			rtr.add(elem);
			++i;
			if (n < i) {
				break;
			}
		}
		return rtr;
	}

	/**
	 * Create a copy of one or more arrays. If there are more than one array, concatenate all of them.
	 */
	@SafeVarargs
	public static <T> T[] concatAll(final T[] first, final T[]... rest) {
		int totalLength = first.length;
		for (final T[] array : rest) {
			totalLength += array.length;
		}
		final T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (final T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static String convertStreamToString(final InputStream is) {
		@SuppressWarnings("resource")
		final Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/**
	 * Determines if an {@link Iterable} is sorted according to the natural comparator. The order of objects that are
	 * equal according to the natural ordering is irrelevant.
	 *
	 * @param iterable
	 *            The {@link Iterable} that should be checked.
	 * @return true if the {@link Iterable} is sorted, false otherwise.
	 */
	public static <T extends Comparable<? super T>> boolean isSorted(final Iterable<T> iterable) {
		final Iterator<T> iter = iterable.iterator();
		if (!iter.hasNext()) {
			// empty iterables are always sorted
			return true;
		}
		T last = iter.next();
		while (iter.hasNext()) {
			final T current = iter.next();
			final int cmp = last.compareTo(current);
			if (cmp > 0) {
				return false;
			}
			last = current;
		}
		return true;
	}

	/**
	 * Determines if an {@link Iterable} is sorted according to the provided {@link Comparator}omparator. The order of
	 * objects that are equal according to the comparator is irrelevant.
	 *
	 * @param iterable
	 *            The {@link Iterable} that should be checked.
	 * @param comparator
	 *            The comparator that should be used for the sorting check.
	 * @return true if the {@link Iterable} is sorted, false otherwise.
	 */
	public static <T extends Comparable<? super T>> boolean isSorted(final Iterable<T> iterable,
			final Comparator<T> comparator) {
		final Iterator<T> iter = iterable.iterator();
		if (!iter.hasNext()) {
			// empty iterables are always sorted
			return true;
		}
		T last = iter.next();
		while (iter.hasNext()) {
			final T current = iter.next();
			if (comparator.compare(last, current) >= 0) {
				return false;
			}
			last = current;
		}
		return true;
	}

	/**
	 * @return a new {@link Map} that contains all key-value pairs of map whose key is contained in filter.
	 */
	public static <K, V> Map<K, V> constructFilteredMap(final Map<K, V> map, final Collection<K> filter) {
		final HashMap<K, V> result = new HashMap<>();
		for (final K key : filter) {
			final V value = map.get(key);
			if (value != null) {
				result.put(key, value);
			}
		}
		return result;
	}

	/**
	 * Construct a new {@link Set} that contains the elements of a given Iterable.
	 */
	public static <E> Set<E> constructHashSet(final Iterable<E> iterable) {
		final HashSet<E> result = new HashSet<>();
		for (final E element : iterable) {
			result.add(element);
		}
		return result;
	}

	/**
	 * Converts a number of bytes to a human readable String containing the byte number as the highest compatible unit.
	 *
	 * @param bytes
	 *            A number of bytes
	 * @param si
	 *            true iff SI units should be used (base 1000, without the "i")
	 * @return
	 */
	public static String humanReadableByteCount(final long bytes, final boolean si) {
		final int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		final int exp = (int) (Math.log(bytes) / Math.log(unit));
		final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static String humanReadableNumber(final long number) {
		final int unit = 1000;
		if (number < unit) {
			return Long.toString(number);
		}
		final int exp = (int) (Math.log(number) / Math.log(unit));
		final String pre = String.valueOf("KMGTPE".charAt(exp - 1));
		return String.format("%.1f %s", number / Math.pow(unit, exp), pre);
	}

	/***
	 * Returns a String representation of a collection by calling toString on each object in the collection.
	 *
	 * @param collection
	 * @param delimiter
	 * @return
	 */
	public static String join(final Collection<?> collection, final String delimiter) {
		final StringBuilder builder = new StringBuilder();
		final Iterator<?> iter = collection.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());
			if (!iter.hasNext()) {
				break;
			}
			builder.append(delimiter);
		}
		return builder.toString();
	}

	/**
	 * Returns a String representation of time as a fraction of the largest whole unit.
	 *
	 * I.e. 1001ms becomes 1,001s, 25h become 1,041d.
	 *
	 * @param time
	 *            The amount of time
	 * @param unit
	 *            The unit of the amount.
	 * @param decimal
	 *            The decimal accurracy of the ouptut.
	 * @return A String with unit symbol.
	 */
	public static String humanReadableTime(final long time, final TimeUnit unit, final int decimal) {
		return humanReadableTime((double) time, unit, decimal);
	}

	/**
	 * Returns a String representation of time as a fraction of the largest whole unit.
	 *
	 * I.e. 1001ms becomes 1,001s, 25h become 1,041d.
	 *
	 * @param time
	 *            The amount of time
	 * @param unit
	 *            The unit of the amount.
	 * @param decimal
	 *            The decimal accurracy of the ouptut.
	 * @return A String with unit symbol.
	 */
	public static String humanReadableTime(final double time, final TimeUnit unit, final int decimal) {
		final String[] units = { "ns", "µs", "ms", "s", "m", "h", "d" };

		switch (unit) {
		case DAYS:
			return String.format("%." + decimal + "f %s", time, units[6]);
		case HOURS:
			if (time > 24) {
				return humanReadableTime(time / 24.0, TimeUnit.DAYS, decimal);
			}
			return String.format("%." + decimal + "f %s", time, units[5]);
		case MINUTES:
			if (time > 60) {
				return humanReadableTime(time / 60.0, TimeUnit.HOURS, decimal);
			}
			return String.format("%." + decimal + "f %s", time, units[4]);
		case SECONDS:
			if (time > 60) {
				return humanReadableTime(time / 60.0, TimeUnit.MINUTES, decimal);
			}
			return String.format("%." + decimal + "f %s", time, units[3]);
		case MILLISECONDS:
			if (time > 1000) {
				return humanReadableTime(time / 1000.0, TimeUnit.SECONDS, decimal);
			}
			return String.format("%." + decimal + "f %s", time, units[2]);
		case MICROSECONDS:
			if (time > 1000) {
				return humanReadableTime(time / 1000.0, TimeUnit.MILLISECONDS, decimal);
			}
			return String.format("%." + decimal + "f %s", time, units[1]);
		case NANOSECONDS:
			if (time > 1000) {
				return humanReadableTime(time / 1000.0, TimeUnit.MICROSECONDS, decimal);
			}
			return String.format("%." + decimal + "f %s", time, units[0]);
		default:
			throw new UnsupportedOperationException(unit + " TimeUnit not yet implemented");
		}
	}

	/**
	 * Filter Collection to all elements that are subclasses of clazz.
	 */
	@SuppressWarnings("unchecked")
	public static <E> Collection<E> filter(final Collection<?> iterable, final Class<E> clazz) {
		final ArrayList<E> filteredList = new ArrayList<>();
		for (final Object e : iterable) {
			if (clazz.isAssignableFrom(e.getClass())) {
				filteredList.add((E) e);
			}
		}
		return filteredList;
	}

	@FunctionalInterface
	public interface IReduce<T, K> {
		T reduce(K entry);
	}

	@FunctionalInterface
	public interface IMapReduce<T, K> {
		T reduce(T lastValue, K entry);
	}

	@FunctionalInterface
	private interface IWriterConsumer {
		void consume(Writer fw) throws IOException;
	}

	public static String getStackTrace(final Throwable t) {
		final StringBuilder sb = new StringBuilder();
		for (final StackTraceElement elem : t.getStackTrace()) {
			sb.append(String.format("%s%n", elem.toString()));
		}
		return sb.toString();
	}
}
