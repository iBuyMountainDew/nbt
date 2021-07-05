package dev.dewy.nbt.tags;

import dev.dewy.nbt.Tag;
import dev.dewy.nbt.TagType;
import dev.dewy.nbt.utils.CompressionType;
import dev.dewy.nbt.utils.Pair;
import dev.dewy.nbt.utils.ReadFunction;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of the compound tag. A map in its raw form.
 *
 * @author dewy
 */
public class CompoundTag implements Tag {
    private Map<String, Tag> value;

    /**
     * Reads a {@link CompoundTag} from a {@link DataInput} stream.
     */
    public static final ReadFunction<DataInput, CompoundTag> read = input -> {
        Map<String, Tag> tags = new HashMap<>();

        while (true) {
            TagType type = TagType.fromByte(input.readByte());

            if (type == TagType.END) { // 0x00 at the end of the tag
                break;
            }

            tags.put(input.readUTF(), ReadFunction.of(type).read(input));
        }

        return new CompoundTag(tags);
    };

    /**
     * Constructs a new empty compound tag.
     */
    public CompoundTag() {
        this.value = new HashMap<>();
    }

    /**
     * Constructs a new compound tag with a given value.
     *
     * @param value The value to be contained within the tag.
     * @throws IllegalArgumentException If the value parameter is null.
     */
    public CompoundTag(Map<String, Tag> value) {
        if (value == null) {
            throw new IllegalArgumentException("Value of compound tag cannot be null.");
        }

        this.value = value;
    }

    /**
     * Returns the map value contained inside the tag.
     *
     * @return The map value contained inside the tag.
     */
    public Map<String, Tag> getValue() {
        return value;
    }

    /**
     * Sets the map value contained inside the tag.
     *
     * @param value The new map value to be contained inside this tag.
     */
    public void setValue(Map<String, Tag> value) {
        if (value == null) {
            throw new IllegalArgumentException("Value of compound tag cannot be null.");
        }

        this.value = value;
    }

    @Override
    public TagType getType() {
        return TagType.COMPOUND;
    }

    @Override
    public void write(DataOutput output) throws IOException {
        for (Map.Entry<String, Tag> tag : this.value.entrySet()) {
            output.writeByte(tag.getValue().getType().getId());

            if (tag.getValue().getType() != TagType.END) {
                output.writeUTF(tag.getKey());
                tag.getValue().write(output);
            }
        }

        output.writeByte(TagType.END.getId()); // 0x00
    }

    /**
     * Write the compound tag to a {@link DataOutput} stream as the root compound with a name of its own.
     *
     * @param output The stream to write to.
     * @param rootName The root compound's name.
     * @throws IOException If any IO error occurs.
     */
    public void writeRoot(DataOutput output, String rootName) throws IOException {
        output.writeByte(TagType.COMPOUND.getId());
        output.writeUTF(rootName);

        write(output);
    }

    /**
     * Write the compound tag to a {@link File} with a name of its own, using a given compression scheme.
     *
     * @param rootName The root compound's name.
     * @param file The file to be written to.
     * @param compression The compression to be applied.
     * @throws IOException If any IO error occurs.
     */
    public void writeRootToFile(String rootName, File file, CompressionType compression) throws IOException {
        DataOutputStream out = compression == CompressionType.GZIP
                ? new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
                : new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        writeRoot(out, rootName);

        out.close();
    }

    @Override
    public ReadFunction<DataInput, CompoundTag> getReader() {
        return read;
    }

    /**
     * Returns true if this compound tag contains no entries.
     *
     * @return True if this compound tag contains no entries.
     */
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    /**
     * Returns the number of entries in this compound tag.
     *
     * @return The number of entries in this compound tag.
     */
    public int size() {
        return this.value.size();
    }

    /**
     * Associates the specified tag value with the specified name in this compound tag.
     * If the compound tag previously contained a mapping for the name, the old tag value is replaced by the specified tag value.
     *
     * @param name Name with which the specified tag value is to be associated.
     * @param tag Tag value to be associated with the specified name.
     * @return The previous tag value associated with name, or null if there was no mapping for name.
     */
    public Tag put(String name, Tag tag) {
        if (name == null || tag == null) {
            throw new IllegalArgumentException("Tag (name) must not be null to put.");
        }

        return this.value.put(name, tag);
    }

    /**
     * If the specified name is not already associated with a tag value (or is mapped to null) associates
     * it with the given tag value and returns null, else returns the current tag value. See Map.putIfAbsent.
     *
     * @param name Name with which the specified tag value is to be associated.
     * @param tag Value to be associated with the specified tag.
     * @return The previous tag value associated with the specified name, or null if there was no mapping for the name.
     */
    public Tag putIfAbsent(String name, Tag tag) {
        if (name == null || tag == null) {
            throw new IllegalArgumentException("Tag (name) must not be null to putIfAbsent.");
        }

        return this.value.putIfAbsent(name, tag);
    }

    /**
     * Returns the value to which the specified name is mapped, or null if this compound tag contains no mapping for the name.
     *
     * @param name The name whose associated tag value is to be returned.
     * @return The value to which the specified name is mapped, or null if this compound tag contains no mapping for the name.
     */
    public Tag get(String name) {
        return this.value.get(name);
    }

    /**
     * Removes the mapping for a name from this compound tag if it is present.
     *
     * @param name Name whose mapping is to be removed from the compound tag.
     * @return The previous value associated with name, or null if there was no mapping for name.
     */
    public Tag remove(String name) {
        return this.value.remove(name);
    }

    /**
     * Removes the entry for a specified named tag. Must be equal in its name and the tag itself to be removed.
     *
     * @param name Name with which the specified tag is associated.
     * @param tag Tag expected to be associated with the specified name.
     * @return True if the entry was removed.
     */
    public boolean remove(String name, Tag tag) {
        return this.value.remove(name, tag);
    }

    /**
     * Returns true if this compound tag contains a tag with the specified name.
     *
     * @param name The name whose presence is to be tested.
     * @return True if this compound tag contains a tag with the specified name.
     */
    public boolean contains(String name) {
        return this.value.containsKey(name);
    }

    /**
     * Returns true if this compound tag contains the specified tag, regardless of its name.
     *
     * @param tag The tag whose presence is to be tested.
     * @return True if this compound tag contains the specified tag, regardless of its name.
     */
    public boolean contains(Tag tag) {
        return this.value.containsValue(tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompoundTag that = (CompoundTag) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Reads a root compound (full NBT structure) from a {@link DataInput} stream.
     *
     * @param input The {@link DataInput} stream to read from.
     * @throws IOException if any kind of IO error occurs.
     * @return The root compound read from the {@link DataInput} stream.
     */
    public static CompoundTag readRoot(DataInput input) throws IOException {
        return readNamedRoot(input).getRight();
    }

    /**
     * Reads a root compound (full NBT structure) from a {@link DataInput} stream, with its name attached.
     *
     * @param input The {@link DataInput} stream to read from.
     * @throws IOException if any kind of IO error occurs.
     * @return A {@link Pair} with the name of the root tag on the left and the root tag object on the right.
     */
    public static Pair<String, CompoundTag> readNamedRoot(DataInput input) throws IOException {
        if (input.readByte() != TagType.COMPOUND.getId()) {
            throw new IOException("Root tag must be a compound tag.");
        }

        return new Pair<>(input.readUTF(), read.read(input));
    }

    /**
     * Reads a root compound (full NBT structure) from a {@link File} with a given kind of compression.
     *
     * @param file The file to read from.
     * @param compression The compression of the file.
     * @throws IOException if any kind of IO error occurs.
     * @return The root compound read from the file.
     */
    public static CompoundTag readRootFromFile(File file, CompressionType compression) throws IOException {
        return readNamedRootFromFile(file, compression).getRight();
    }

    /**
     * Reads a root compound (full NBT structure) from a {@link File} with a given kind of compression, with its name attached.
     *
     * @param file The file to read from.
     * @param compression The compression of the file.
     * @throws IOException if any kind of IO error occurs.
     * @return A {@link Pair} with the name of the root tag on the left and the root tag object on the right.
     */
    public static Pair<String, CompoundTag> readNamedRootFromFile(File file, CompressionType compression) throws IOException {
        DataInputStream in = compression == CompressionType.GZIP
                ? new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))
                : new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        Pair<String, CompoundTag> result = readNamedRoot(in);

        in.close();
        return result;
    }
}