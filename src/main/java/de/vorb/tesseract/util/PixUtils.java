package de.vorb.tesseract.util;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import org.bridj.Pointer;

import de.vorb.tesseract.bridj.Tesseract.Pix;

/**
 * Utility methods for Pix image objects.
 * 
 * @author Paul Vorbach
 */
public class PixUtils {
  private PixUtils() {
  }

  private static final int BYTE_BITS = 8;
  private static final int INT_BITS = 4 * BYTE_BITS;

  /**
   * @param bufferedImage
   * @return
   */
  public static Pix bufferedImageToPix(BufferedImage bufferedImage) {
    final Pix pix = new Pix();

    pix.w(bufferedImage.getWidth());
    pix.h(bufferedImage.getHeight());

    switch (bufferedImage.getType()) {
    case BufferedImage.TYPE_BYTE_BINARY:
      pix.d(1);
      pix.spp(1);
      break;
    case BufferedImage.TYPE_BYTE_GRAY:
      pix.d(8);
      pix.spp(1);
      break;
    default:
      // if the given image is neither binary or grayscale, convert it to
      // grayscale
      final ColorConvertOp op = new ColorConvertOp(
          ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
      final BufferedImage gray = new BufferedImage(bufferedImage.getWidth(),
          bufferedImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      op.filter(bufferedImage, gray);

      // replace input image with grayscale version
      bufferedImage = gray;

      pix.d(8);
      pix.spp(1);
    }

    // calculate words (ints) per line
    pix.wpl((pix.w() * pix.d() + 31) / 32);

    final DataBufferByte dataBuf =
        (DataBufferByte) bufferedImage.getData().getDataBuffer();
    ByteBuffer dataBufBytes = ByteBuffer.wrap(dataBuf.getData());

    // cast this bytebuffer to an int buffer
    final Pointer<Integer> data = Pointer.pointerToBytes(dataBufBytes).as(
        Integer.TYPE);

    pix.data(data);

    return pix;
  }

  /**
   * Takes a Pix as used by Tesseract and converts it to a BufferedImage.
   * 
   * @param pix
   *          Pix
   * @return BufferedImage
   */
  public static BufferedImage pixToBufferedImage(Pix pix) {
    final byte[] buf = convertPixToByteBuffer(pix.data(), pix.w(), pix.h(),
        pix.wpl());

    final DataBufferByte dataBuf = new DataBufferByte(buf, buf.length);

    // ... and a writable raster
    final WritableRaster raster = Raster.createPackedRaster(dataBuf, pix.w(),
        pix.h(), pix.d(), new Point(0, 0));

    // determine the color model
    // This is partially taken from java.awt.image.BufferedImage
    final ColorModel cm;

    switch (pix.d()) {
    case 1:
      final byte[] arr = { (byte) 0xFF, (byte) 0x00 };

      cm = new IndexColorModel(1, 2, arr, arr, arr);
      break;
    case 8:
      final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
      int[] nBits = { 8 };
      cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE,
          DataBuffer.TYPE_INT);
      break;
    default:
      throw new IllegalArgumentException(
          "Only binary and grayscale images allowed.");
    }

    // create and return the buffered image
    return new BufferedImage(cm, raster, false, new Hashtable<>());
  }

  private static byte[] convertPixToByteBuffer(Pointer<Integer> pixData,
      final int width, final int height, final int wpl) {
    // size of the underlying int[]
    final int bufSize = wpl * height * 4;

    final byte[] bufData = new byte[bufSize];

    final ByteBuffer buf = ByteBuffer.wrap(bufData);

    final boolean notMisaligned = width % 32 == 0;
    final int misalignment;
    if (notMisaligned)
      misalignment = 0;
    else
      misalignment = (width % 32 + 7) / 8;

    final int bulkSize;
    if (notMisaligned)
      bulkSize = wpl * 4;
    else
      bulkSize = (wpl - 1) * 4;

    // copy over data
    for (int y = 0; y < height; ++y, pixData = pixData.next(wpl)) {
      final byte[] bulk = pixData.getBytes(bulkSize);

      // reorder the bytes
      for (int b = 0; b < bulkSize; b += 4) {
        final byte b0 = bulk[b];
        final byte b1 = bulk[b + 1];
        bulk[b] = bulk[b + 3];
        bulk[b + 1] = bulk[b + 2];
        bulk[b + 2] = b1;
        bulk[b + 3] = b0;
      }

      buf.put(bulk, 0, bulkSize);

      if (notMisaligned)
        continue;

      // append the last int of a line
      final byte[] lastIntOfLine = pixData.next(wpl - 1).getBytes(4);
      for (int b = misalignment; b > 0; --b) {
        buf.put(lastIntOfLine[b]);
      }
    }

    return bufData;
  }
}
