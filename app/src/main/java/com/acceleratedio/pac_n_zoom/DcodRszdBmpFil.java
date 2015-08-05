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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DcodRszdBmpFil {

	public Bitmap DcodRszdBmpFil(String orgFil, BitmapFactory.Options options)
	{
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(orgFil, options);
		options.inSampleSize = calInSampleSize(options);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(orgFil, options);
	}

	public int calInSampleSize(BitmapFactory.Options options) {
		
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		int pxl_nmbr = height * width;

		while (pxl_nmbr > 500000) {
			pxl_nmbr /= 4;
			inSampleSize *= 2;			
		}

		return inSampleSize;
	}
}	
