
package edu.virginia.lib.ole.akubra.utilities;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.ByteStreams.copy;
import static edu.virginia.lib.ole.akubra.Constants.CHUNK;
import static java.lang.Short.parseShort;
import static java.lang.System.exit;
import static java.lang.System.out;
import static java.net.URI.create;
import static java.net.URLDecoder.decode;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.virginia.lib.ole.akubra.FilePrefixMapper;

public class FSBlobStoreSplitter {

    private static class FSBlobStoreSplitterImpl {

        private final BlobStore original, left, right;

        private final ExecutorService threadpool = newFixedThreadPool(1);

        static boolean clean = true;

        public FSBlobStoreSplitterImpl(final BlobStore original, final BlobStore left,
                final BlobStore right) {
            this.original = original;
            this.left = left;
            this.right = right;
        }

        public void split() throws UnsupportedOperationException, IOException {
            final BlobStoreConnection orig = original.openConnection(null, null);
            final BlobStoreConnection l = left.openConnection(null, null);
            final BlobStoreConnection r = right.openConnection(null, null);
            URI blobId;
            final Iterator<URI> blobList = orig.listBlobIds(null);
            while (blobList.hasNext()) {
                blobId = blobList.next();
                print("Entering blobList.hasNext()");
                // we use an URL decode here to account for some odd behavior from
                // FSBlobStore FSBlobIdIterator
                final String blobIdString = decode(blobId.toString(), UTF_8.toString());
                print("Using blobIdString: " + blobIdString);
                blobId = create(blobIdString);
                final String newBlobIdString = blobIdString.substring(CHUNK + 1);
                print("Using newBlobIdString: " + newBlobIdString);
                final short front = parseShort((blobIdString.substring(0, CHUNK)), 2);
                print("Using front: " + front);
                final URI newBlobId = create(newBlobIdString);

                final Blob inBlob = orig.getBlob(blobId, null);
                if (front % 2 == 0) {
                    threadpool.execute(new CopyBlob(inBlob, l, newBlobId));
                } else {
                    threadpool.execute(new CopyBlob(inBlob, r, newBlobId));
                }
                if (!clean) {
                    print("Failed to finish split cleanly!");
                    exit(1);
                }
            }
            threadpool.shutdown();
        }

        private static class CopyBlob implements Runnable {

            private final Blob inBlob;

            private final BlobStoreConnection target;

            private final URI newBlobId;

            CopyBlob(final Blob inBlob, final BlobStoreConnection target, final URI newBlobId) {
                this.inBlob = inBlob;
                this.target = target;
                this.newBlobId = newBlobId;
            }

            @Override
            public void run() {
                print("Copying inBlob " + inBlob.getId() + " to target store "
                        + target.getBlobStore().getId() + " under new URI "
                        + newBlobId);

                try (InputStream in = inBlob.openInputStream();
                        OutputStream out = target.getBlob(newBlobId, null).openOutputStream(inBlob.getSize(), true)) {
                    copy(in, out);
                } catch (final IOException e) {
                    clean = false;
                    e.printStackTrace();
                }
            }
        }

        static void print(final String s) {
            out.println(s);
        }
    }

    public static void main(final String[] args) throws UnsupportedOperationException, IOException {

        final Options options = new Options();
        options.addOption(new Option("r", "right", true,
                "Right-hand output BlobStore filepath (required)"));
        options.addOption(new Option("l", "left", true,
                "Left-hand output BlobStore filepath (required)"));
        options.addOption(new Option("o", "original", true,
                "Original (input) BlobStore filepath (required)"));
        options.addOption(new Option("h", "help", false,
                "Get this help"));

        final CommandLineParser parser = new GnuParser();
        final HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e1) {
            System.out.println("Failed to parse options: ");
            e1.printStackTrace();
        }

        // automatically generate the help statement
        if (cmd.hasOption("h")) {
            formatter.printHelp("TwoStore BlobSplitter", options, true);
            System.exit(0);
        }
        if (!cmd.hasOption("r")) {
            System.out.println("Missing right-hand output BlobStore filepath!");
            formatter.printHelp("TwoStore BlobSplitter", options, true);
            System.exit(1);
        }
        if (!cmd.hasOption("l")) {
            System.out.println("Missing left-hand output BlobStore filepath!");
            formatter.printHelp("TwoStore BlobSplitter", options, true);
            System.exit(1);
        }
        if (!cmd.hasOption("o")) {
            System.out.println("Missing original (input) BlobStore filepath!");
            formatter.printHelp("TwoStore BlobSplitter", options, true);
            System.exit(1);
        }

        final String original_location = cmd.getOptionValue("o");
        final String left_location = cmd.getOptionValue("l");
        final String right_location = cmd.getOptionValue("r");

        final BlobStore fsoriginal = new FSBlobStore(URI.create("fsoriginal"),
                new File(original_location));
        final BlobStore fsleft = new FSBlobStore(URI.create("fsleft"), new File(
                left_location));
        final BlobStore fsright = new FSBlobStore(URI.create("fsright"), new File(
                right_location));

        final IdMapper fileprefixmapper = new FilePrefixMapper();

        final BlobStore original = new IdMappingBlobStore(URI.create("original"),
                fsoriginal, fileprefixmapper);
        final BlobStore left = new IdMappingBlobStore(URI.create("left"), fsleft,
                fileprefixmapper);
        final BlobStore right = new IdMappingBlobStore(URI.create("right"), fsright,
                fileprefixmapper);

        new FSBlobStoreSplitterImpl(original, left, right).split();

    }
}
