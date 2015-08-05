/*
 * Copyright (C) 2014 Accelerated I/O, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acceleratedio.pac_n_zoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class DrawSVG {
	
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public ArrayList<ImageView> DrawSVG(Context context, ImageView orgnlImageView, 
		RelativeLayout rel_anm_lo) {

    paint.setColor(Color.WHITE);
    paint.setStyle(Paint.Style.FILL);
		LoadSVG.SVGData data = AnimActivity.svg_data;		
		ArrayList<ImageView> imgViews = new ArrayList<ImageView>();
		imgViews.add(orgnlImageView); 

		if (data.symbl != null) {

			ArrayList<String> sprt_ordr = data.svg.ordr;
			int sym_mbr = 0;
			Bitmap bitmap;
			Canvas canvas;

			// - Loop through the sprites 
			int sprt_nmbr = sprt_ordr.size();
			data.svg.initScl = new float[sprt_nmbr];
			float[] initScl = data.svg.initScl;

			for (int sprt_mbr = 1; sprt_mbr < sprt_nmbr; sprt_mbr += 1) {

				String sprt_id = sprt_ordr.get(sprt_mbr); // e.g., id="g2_0"

				if (Integer.parseInt(sprt_id.substring(sprt_id.indexOf('_') + 1)) > 0) {
					// The symbol is already drawn; replicate the view	
					String init_sprt = sprt_id.substring(0, sprt_id.indexOf('_') + 1) + '0';
					String svg_ordr = data.svg.svg_ordr;
					String cnt_str = svg_ordr.substring(0, svg_ordr.indexOf(init_sprt)); 
					ImageView init_vw = imgViews.get(StringUtils.countMatches(cnt_str, ","));
					Bitmap initBmp = ((BitmapDrawable) init_vw.getDrawable()).getBitmap();
					
					bitmap = 
						Bitmap.createBitmap(initBmp.getWidth(), initBmp.getHeight(), initBmp.getConfig());

					canvas = new Canvas(bitmap);
					canvas.save(Canvas.MATRIX_SAVE_FLAG);
					xfrmInit crt_sprt = getInitSpriteAttrib(sprt_id);
					canvas.scale(crt_sprt.scl, crt_sprt.scl);
					initScl[sprt_mbr] = crt_sprt.scl;
					canvas.translate(crt_sprt.trans[0], crt_sprt.trans[1]);
				} else {
					// The symbol needs to be drawn; a new view	is used
					bitmap = getCreatBmp(rel_anm_lo);
					canvas = new Canvas(bitmap);
					canvas.save(Canvas.MATRIX_SAVE_FLAG);

					// - Set the init values
					xfrmInit crt_sprt = getInitSpriteAttrib(sprt_id);
					canvas.scale(crt_sprt.scl, crt_sprt.scl);
					initScl[sprt_mbr] = crt_sprt.scl;
					canvas.translate(crt_sprt.trans[0], crt_sprt.trans[1]);

					// - Draw the bitmap
					LoadSVG.symbol crt_sym = data.symbl.get(sym_mbr);
					ArrayList<LoadSVG.path> pths = crt_sym.pths;
					int pth_nmbr = pths.size(); 	
				
					// Loop through the paths
					for (int pth_mbr = 0; pth_mbr < pth_nmbr; pth_mbr += 1) {

						LoadSVG.path crt_pth = pths.get(pth_mbr);
						final Path path = new Path();
						final Paint paint = new Paint();
						/* Debug
						if (pth_mbr + 1 == pth_nmbr) {

							String log_str = "Paths: pth_mbr = " +
								String.valueOf(pth_mbr) + "; color = " + crt_pth.clr;

							Log.d("DrawSVG", log_str);
						}
						*/
						paint.setColor(Color.parseColor(crt_pth.clr));
						paint.setAntiAlias(true);
						paint.setStyle(Paint.Style.FILL_AND_STROKE);
						ld_pth_pnts(crt_pth.pth, path); 
						path.close();
						path.setFillType(Path.FillType.EVEN_ODD);
						canvas.drawPath(path, paint);
					}

					canvas.restore();
					sym_mbr += 1;
				}
					
				ImageView iv = new ImageView(context);
				iv.setImageBitmap(bitmap);

				RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

				iv.draw(canvas);
				iv.setLayoutParams(rlp);
				iv.setBackgroundColor(Color.TRANSPARENT);
				imgViews.add(iv); 
			} // sprites
		} // if symbol

		return imgViews;
	} // DrawSVG()

	public Bitmap getCreatBmp(RelativeLayout rel_anm_lo) {
/*
		Drawable oldDrawable = rel_anm_lo.getDrawable();
    BitmapDrawable oldBitmapDrawable = null;

    if (oldDrawable instanceof TransitionDrawable)
    {
      TransitionDrawable oldTransitionDrawable = (TransitionDrawable) oldDrawable;
      oldBitmapDrawable = (BitmapDrawable) (oldTransitionDrawable).getDrawable(1);
			return oldBitmapDrawable.getBitmap();
    }
    else if (oldDrawable instanceof BitmapDrawable)
    {
      oldBitmapDrawable = (BitmapDrawable) oldDrawable;
			return oldBitmapDrawable.getBitmap();
    }
*/
		return Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888);		
	}

  public static void ld_pth_pnts(ArrayList<Integer[]> pnts, Path path) {

		int pnt_nmbr = pnts.size();
		Integer[] crt_pnt = pnts.get(0);
		path.moveTo(crt_pnt[0], crt_pnt[1]);

		// Loop through the points of a path
		for (int pnt_mbr = 1; pnt_mbr < pnt_nmbr; pnt_mbr += 1) {

			crt_pnt = pnts.get(pnt_mbr);
			path.lineTo(crt_pnt[0], crt_pnt[1]);
		}
  }

	class xfrmInit {
    String id; // "anm_g2_0_f0"
		float scl; // Percent
		Integer[] trans = new Integer[2]; // Pixels
	}
	
	public xfrmInit getInitSpriteAttrib(String sprt_id) {
		// Loop through the Xfrm list until first display of the passed sprite
		LoadSVG.SVGData data = AnimActivity.svg_data; 
		xfrmInit init_xfrm = null;

		if (data.xfm != null) {

			int frm_nmbr = data.frm.size();
			int xfm_nmbr = data.xfm.size();
			init_xfrm = new xfrmInit();
			int frm_mbr = 0;	

			// Loop through the frames
			while (frm_mbr < frm_nmbr) {

				LoadSVG.frame crt_frm = data.frm.get(frm_mbr);
				
				if (crt_frm.frm_ordr.indexOf(sprt_id) >= 0) break;

				frm_mbr += 1;
			}
			
			String xfrm_id = "anm_" + sprt_id + "_f" + Integer.toString(frm_mbr); // e.g., "anm_g2_0_f0"
			LoadSVG.xfrm crt_xfm = null;

			// Loop through the Xfrms
			for (int xfm_mbr = 0; xfm_mbr < xfm_nmbr; xfm_mbr += 1) {

				crt_xfm = data.xfm.get(xfm_mbr);

				if (crt_xfm.id.equals(xfrm_id)) break;
			}

			init_xfrm.id = xfrm_id;
			init_xfrm.scl = (float) crt_xfm.scl_bgn / 100;
			Integer[] init_pth = crt_xfm.mov_path.get(0);
			init_xfrm.trans[0] = init_pth[0];
			init_xfrm.trans[1] = init_pth[1];
			return init_xfrm;
		}

		return init_xfrm;
	}
}
