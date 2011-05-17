package edu.virginia.lib.ole.akubra.utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.apache.commons.io.IOUtils;

import edu.virginia.lib.ole.akubra.Constants;
import edu.virginia.lib.ole.akubra.FilePrefixMapper;

public class FSBlobStoreSplitter {

	private class FSBlobStoreSplitterImpl {

		private BlobStore original, left, right;

		private ExecutorService threadpool = Executors.newFixedThreadPool(1);

		private boolean clean = true;

		public FSBlobStoreSplitterImpl(BlobStore original, BlobStore left,
				BlobStore right) {
			this.original = original;
			this.left = left;
			this.right = right;
		}

		public void split() throws UnsupportedOperationException, IOException {
			BlobStoreConnection orig = original.openConnection(null, null);
			BlobStoreConnection l = left.openConnection(null, null);
			BlobStoreConnection r = right.openConnection(null, null);
			URI blobId = null;
			Iterator<URI> blobList = orig.listBlobIds(null);
			while (blobList.hasNext()) {
				blobId = blobList.next();
				print("Entering blobList.hasNext()");
				// we use an URL decode here to account for some odd behavior
				// from
				// FSBlobStore FSBlobIdIterator
				String blobIdString = URLDecoder.decode(blobId.toString(),
						"UTF-8");
				print("Using blobIdString: " + blobIdString);
				blobId = URI.create(blobIdString);
				String newBlobIdString = blobIdString
						.substring(Constants.CHUNK + 1);
				print("Using newBlobIdString: " + newBlobIdString);
				Short front = Short.parseShort(
						(blobIdString.substring(0, Constants.CHUNK)), 2);
				print("Using front: " + front);
				URI newBlobId = URI.create(newBlobIdString);

				Blob inBlob = orig.getBlob(blobId, null);
				if (front % 2 == 0) {
					threadpool.execute(new copyBlob(inBlob, l, newBlobId));
				} else {
					threadpool.execute(new copyBlob(inBlob, r, newBlobId));
				}
				if (!clean) {
					print("Failed to finish split!");
					System.exit(1);
				}
			}
			threadpool.shutdown();
		}

		private class copyBlob implements Runnable {
			private final Blob inBlob;
			private final BlobStoreConnection target;
			private final URI newBlobId;

			copyBlob(Blob inBlob, BlobStoreConnection target, URI newBlobId) {
				this.inBlob = inBlob;
				this.target = target;
				this.newBlobId = newBlobId;
			}

			public void run() {
				print("Copying inBlob " + inBlob.getId() + " to target store "
						+ target.getBlobStore().getId() + " under new URI "
						+ newBlobId);
				InputStream in;
				OutputStream out;
				try {
					in = inBlob.openInputStream();
					out = target.getBlob(newBlobId, null).openOutputStream(
							inBlob.getSize(), true);
					IOUtils.copy(in, out);
					out.close();
					out = null;
					in.close();
					in = null;
				} catch (Exception e) {
					clean = false;
					e.printStackTrace();
				}

			}

		}

		private void print(String s) {
			System.out.println(s);
		}
	}

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption(new Option("r", "right", true,
				"Right-hand output BlobStore filepath (required)"));
		options.addOption(new Option("l", "left", true,
				"Left-hand output BlobStore filepath (required)"));
		options.addOption(new Option("o", "original", true,
				"Original (input) BlobStore filepath (required)"));
		options.addOption(new Option("h", "help", false,
		"Get this help"));
		
		CommandLineParser parser = new GnuParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e1) {
			System.out.println("Failed to parse options: ");
			e1.printStackTrace();
		}
		
		// automatically generate the help statement
		if (cmd.hasOption("h")) {
			formatter.printHelp( "TwoStore BlobSplitter", options, true );
			System.exit(0);
		}
		if (!cmd.hasOption("r")) {
			System.out.println("Missing right-hand output BlobStore filepath!");
			formatter.printHelp( "TwoStore BlobSplitter", options, true );
			System.exit(1);
		}
		if (!cmd.hasOption("l")) {
			System.out.println("Missing left-hand output BlobStore filepath!");
			formatter.printHelp( "TwoStore BlobSplitter", options, true );
			System.exit(1);
		}
		if (!cmd.hasOption("o")) {
			System.out.println("Missing original (input) BlobStore filepath!");
			formatter.printHelp( "TwoStore BlobSplitter", options, true );
			System.exit(1);
		}

		String original_location = cmd.getOptionValue("o");
		String left_location = cmd.getOptionValue("l");
		String right_location = cmd.getOptionValue("r");

		BlobStore fsoriginal = new FSBlobStore(URI.create("fsoriginal"),
				new File(original_location));
		BlobStore fsleft = new FSBlobStore(URI.create("fsleft"), new File(
				left_location));
		BlobStore fsright = new FSBlobStore(URI.create("fsright"), new File(
				right_location));

		IdMapper fileprefixmapper = new FilePrefixMapper();

		BlobStore original = new IdMappingBlobStore(URI.create("original"),
				fsoriginal, fileprefixmapper);
		BlobStore left = new IdMappingBlobStore(URI.create("left"), fsleft,
				fileprefixmapper);
		BlobStore right = new IdMappingBlobStore(URI.create("right"), fsright,
				fileprefixmapper);

		try {
			new FSBlobStoreSplitter().new FSBlobStoreSplitterImpl(original,
					left, right).split();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
