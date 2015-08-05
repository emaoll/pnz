package com.acceleratedio.pac_n_zoom;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.copyOfRange;

import org.apache.commons.lang3.StringUtils;

public class LoadSVG {
	
	public static SVGData data = null;
	int bgn_idx;
	int fil_len;
	int srch_len;
	int chr_idx;
	String svg_fil;
	
	static class SVGData {
		float g_scl;
		viewPort svg;
		ArrayList<frame> frm = new ArrayList<frame>();
		ArrayList<xfrm> xfm = new ArrayList<xfrm>();
		ArrayList<symbol> symbl = new ArrayList<symbol>();
	}

	static class viewPort {
    String id;
		String svg_ordr; 
		String usrnam;
	 	int width;
		int height; 
		float[] initScl;
		ArrayList<String> ordr = new ArrayList<String>();
	}
	
	static class frame {
    String id;
		String frm_ordr;
	 	int bgn; // Miliseconds
		int end;  // Miliseconds
		Integer[] xfm_idx;
		ArrayList<String> ordr = new ArrayList<String>();
	}

	static class xfrm {
    String id; // "anm_g2_0_f0"
		int scl_bgn; // Percent
		int scl_end; // Percent
		static ArrayList<Integer[]> mov_path = new ArrayList<Integer[]>();
		int rot_bgn; // Degrees
		int rot_end; // Degrees
	}
		
	static class symbol {
    String sym_id; // e.g., id="g2_sprt"
		String g_id; // e.g., id="earth7tan.1741_sel.svg"
		ArrayList<path> pths = new ArrayList<path>();
	}

	static class path {
    String id;
		ArrayList<Integer[]> pth = new ArrayList<Integer[]>();
		String clr;
	}

  public SVGData LoadSVG(String svg_file) {
				
		int symbl_len;
		svg_fil = svg_file;
		chr_idx = 0;
		fil_len = svg_fil.length();
		srch_len = fil_len;
		Log.d("LoadSVG", "Beginning of Parsing");
		char[] svg_chr = new char[fil_len];
		svg_chr = svg_file.toCharArray();
		chr_idx = getStrIdx("<svg", svg_chr);
		bgn_idx =	chr_idx; 
		srch_len = getStrIdx(">", svg_chr);
			
		if (chr_idx >= fil_len) return(data);

		// - Viewport
		chr_idx = getStrIdx(" id=\"", svg_chr);
		data = new SVGData();
		data.svg = new viewPort();
		viewPort svg = data.svg;
		svg.id = cpyToChr('"', svg_chr);
		chr_idx = getStrIdx(" usrnam=\"", svg_chr);
		svg.usrnam  = cpyToChr('"', svg_chr);
		chr_idx = bgn_idx;
		chr_idx = getStrIdx(" width=\"", svg_chr);
		svg.width = Integer.parseInt(cpyToChr('"', svg_chr));
		chr_idx = bgn_idx;
		chr_idx = getStrIdx(" height=\"", svg_chr);
		svg.height = Integer.parseInt(cpyToChr('"', svg_chr));
		chr_idx = bgn_idx;
		chr_idx = getStrIdx(" ordr=\"", svg_chr);
		svg.svg_ordr = cpyToChr('"', svg_chr);
		svg.ordr = new ArrayList<String>(Arrays.asList(svg.svg_ordr.split("\\s*,\\s*")));

		// - Frames
		srch_len = fil_len;
		bgn_idx = getStrIdx("<Frame ", svg_chr);
		srch_len = getStrIdx("<Xfrm ", svg_chr) - 8;
		chr_idx =	bgn_idx - 1; 
		data.frm = new ArrayList<frame>();

		// Loop through the frames
		while (chr_idx < srch_len) {

			frame crt_frm = new frame();
			chr_idx = getStrIdx(" id=\"", svg_chr);
			crt_frm.id = cpyToChr('"', svg_chr);
			crt_frm.bgn = getFrmTim(" bgn=\"", svg_chr);
			crt_frm.end = getFrmTim(" end=\"", svg_chr);
			chr_idx = getStrIdx(" ordr=\"", svg_chr);
			String frm_ordr = cpyToChr('"', svg_chr);
			crt_frm.frm_ordr = frm_ordr; 
			crt_frm.ordr = new ArrayList<String>(Arrays.asList(frm_ordr.split("\\s*,\\s*")));
			int sprt_nmbr = crt_frm.ordr.size();
			crt_frm.xfm_idx = new Integer[sprt_nmbr];

			// Loop through the sprites
			for (int sprt_mbr = 0; sprt_mbr < sprt_nmbr; sprt_mbr += 1)
				crt_frm.xfm_idx[sprt_mbr] = -1; 			

			data.frm.add(crt_frm);
		}

		// - Xfrms
		bgn_idx = getStrIdx("<Xfrm", svg_chr);
		srch_len = fil_len;
		srch_len = getStrIdx("<g ", svg_chr) - 4;
		chr_idx =	bgn_idx; 
		data.xfm = new ArrayList<xfrm>();

		// Loop through the xfrms
		while (chr_idx < srch_len) {

			xfrm crt_xfm = new xfrm();
			chr_idx = getStrIdx(" id=\"", svg_chr);
			crt_xfm.id = cpyToChr('"', svg_chr);
			String xfm_str = cpyToChr('>', svg_chr);
			crt_xfm.scl_bgn = getAttrb(xfm_str, "scl_bgn", 100, 100);
			crt_xfm.scl_end = getAttrb(xfm_str, "scl_end", crt_xfm.scl_bgn, 100);
			crt_xfm.rot_bgn = getAttrb(xfm_str, "rot_bgn", 0, 1);
			crt_xfm.rot_end = getAttrb(xfm_str, "rot_end", crt_xfm.rot_bgn, 1);
			
			String movSrch = " mov_path=\"";
			int bas_idx = xfm_str.indexOf(movSrch);
			String movStr;

			if (bas_idx < 0) movStr = " ";
			else {
				String idxMovStr = xfm_str.substring(bas_idx + 12); 
				movStr = idxMovStr.substring(0, idxMovStr.indexOf('"'));
			}

			crt_xfm.mov_path = ld_pth(movStr); 

			// - Set the xfm_idx
			int idx_f = crt_xfm.id.indexOf('f');
			frame crt_frm = data.frm.get(Integer.parseInt(crt_xfm.id.substring(idx_f + 1)));
			String sprt_id = crt_xfm.id.substring(crt_xfm.id.indexOf('g'), idx_f - 1);
			String cnt_str = crt_frm.frm_ordr.substring(0, crt_frm.frm_ordr.indexOf(sprt_id));
			int xfm_mbr = StringUtils.countMatches(cnt_str, ",");
			crt_frm.xfm_idx[xfm_mbr] = data.xfm.size();

			data.xfm.add(crt_xfm);
		}

		// - Symbols 
		bgn_idx = srch_len + 3;
		srch_len = fil_len;
		chr_idx =	bgn_idx;
		srch_len = getStrIdx("</defs>", svg_chr);
		int srch_symbl = srch_len; 
		chr_idx = bgn_idx;

		// Loop through the symbols
		while (getNxtSymbl(svg_chr)) {
			
			chr_idx = getStrIdx("=\"", svg_chr);
			String symbl_id = cpyToChr('"', svg_chr);

			if (symbl_id.indexOf("sprt") < 0) break; 

			if (!symbl_id.equals("g1_sprt")) { 

				if (data.symbl == null) {
					data.symbl = new ArrayList<symbol>();			
				}
				
				symbol crt_sym = new symbol();
				crt_sym.sym_id = symbl_id;
				srch_len = fil_len;
				crt_sym.g_id = getTagID("<g ", svg_chr);
				crt_sym.pths = new ArrayList<path>();
				srch_len = srch_symbl;
				bgn_idx = chr_idx;
				int srch_pth = getStrIdx("/symbol", svg_chr);
				chr_idx =	bgn_idx;

				// Loop through the paths
				while (getNxtPath(svg_chr)) {
					path crt_pth = new path();
					bgn_idx = chr_idx;
					srch_len = getStrIdx(">", svg_chr);
					chr_idx =	bgn_idx;

					while ((chr_idx = getStrIdx("=\"", svg_chr)) < srch_len) {

						if (svg_chr[chr_idx - 3] == 'd') {
							if (svg_chr[chr_idx - 4] == 'i') crt_pth.id = cpyToChr('"', svg_chr);
							else if (svg_chr[chr_idx - 4] == ' ') {
								chr_idx++;
								crt_pth.pth = ld_svg_pth(svg_chr);
							}
						} else if (svg_chr[chr_idx - 3] == 'e') {
							chr_idx = getStrIdx("#", svg_chr);
							crt_pth.clr = "#FF" + cpyToChr(';', svg_chr).toUpperCase();
						}
					}
					
					crt_sym.pths.add(crt_pth);
					srch_len = srch_pth;
				}
				
				data.symbl.add(crt_sym);

			} else {

				srch_len = fil_len;
				symbl_len = getStrIdx("</symbol>", svg_chr);
			}
		}

		// - Animation scale
	  srch_len = fil_len;
		data.g_scl = 1;
		chr_idx = srch_symbl; 

		while (!getSVGSclID(svg_chr) && chr_idx < srch_len);
			
		if (chr_idx < srch_len) {

			bgn_idx = getStrIdx(" transform=\"", svg_chr);
		
			if (chr_idx < srch_len) {

				srch_len = getStrIdx(">", svg_chr);
				chr_idx =	bgn_idx - 1;
				chr_idx = getStrIdx(" scale(", svg_chr);

				if (chr_idx < srch_len) data.g_scl = Float.parseFloat(cpyToChr(')', svg_chr));
			}
		}

		Log.d("LoadSVG", "End of Parsing");
		return(data);
  }

	private boolean getSVGSclID(char[] svg_chr)
	{
		bgn_idx = getStrIdx("<g ", svg_chr);
	  chr_idx =	bgn_idx - 2;
		chr_idx = getStrIdx(" id=\"", svg_chr);
		return(cpyToChr('"', svg_chr).equals("g_scl"));
	}

	private boolean getNxtSymbl(char[] svg_chr)
	{
		while (true) {

			if (chr_idx > srch_len) return false;	 	
			
			chr_idx = getStrIdx("<", svg_chr);
			String trgt = "symbol";
			bgn_idx	= chr_idx;
			int trgt_idx;

			for (trgt_idx = 0; trgt_idx < 6; trgt_idx++) {

				if (trgt.charAt(trgt_idx) != svg_chr[chr_idx++]) {

					String def_trgt = "/defs";
					chr_idx = bgn_idx;
					int def_idx;

					for (def_idx = 0; def_idx < 5; def_idx++) {
						
						if (def_trgt.charAt(def_idx) != svg_chr[chr_idx++]) break;
					}

					if (def_idx == 5) return false;

					break;
				}
			}

			if (trgt_idx == 6) return true;	
		}
  }

	private boolean getNxtPath(char[] svg_chr)
	{
		chr_idx = getStrIdx("<", svg_chr);
		String trgt = "path";		

		for (int trgt_idx = 0; trgt_idx < 4; trgt_idx++) {

			if (trgt.charAt(trgt_idx) != svg_chr[chr_idx++]) return false;
		}	

		return true;
	}

	/** 
	 * Gets the tag ID
	 *	Parameters:
	 *		trgt: The tag name in the form "<tag_name " 
	 *		
	 *	Modifies Globals: bgn_idx, chr_idx, and srch_len
	 **/
	private String getTagID(String trgt, char[] svg_chr)
	{
		bgn_idx =	getStrIdx(trgt, svg_chr) - 2;
		srch_len = getStrIdx(">", svg_chr);
		chr_idx =	bgn_idx; 
		chr_idx = getStrIdx(" id=\"", svg_chr);
		return(cpyToChr('"', svg_chr));
	}

	private int getStrIdx(String trgt, char[] svg_chr)
	{
		char ltr_0 = trgt.charAt(0);
		int trgt_len = trgt.length();
		int trgt_idx;

		while (true) {

			if (chr_idx >= srch_len) return chr_idx;

			while (svg_chr[chr_idx++]	!= ltr_0) {
				if (chr_idx >= srch_len) return chr_idx;
			}

			for (trgt_idx = 1; trgt_idx < trgt_len; trgt_idx++) {

				if (trgt.charAt(trgt_idx) != svg_chr[chr_idx++]) break;

				if (chr_idx >= srch_len) return chr_idx;
			}	

			if (trgt_idx == trgt_len) break;
		}

		return chr_idx; 
	}

	private String cpyToChr(char trgt, char[] svg_chr)
	{
		int init_idx = chr_idx;

		while (svg_chr[chr_idx++] != trgt) {

			if (chr_idx >= srch_len) break;
		}

		return(new String(copyOfRange(svg_chr, init_idx, chr_idx - 1)));
	}

	private int getFrmTim(String trgt, char[] svg_chr)
	{
		chr_idx = getStrIdx(trgt, svg_chr);

		try {
			return(Math.round(Float.parseFloat(cpyToChr('"', svg_chr)) * 1000));
		} catch(NumberFormatException nfe) {
			return (0); 
		} 
	}

	/** 
	 * Sets the attribute
	 *	Parameters:
	 *		base: The string that holds the tag except for the id
	 *		attrb: The attribute string that was read in from the file
	 *	  dflt: The default value
	 *		mul: The multiplier (100 for percent; 1 for degrees)
	 **/
	private int getAttrb(String base, String attrb, int dflt, int mul) {
   
		int rtrn;
		String srch = " " + attrb + "=\"";
		int bas_idx = base.indexOf(srch);

		if (bas_idx < 0) rtrn = dflt;
		else {
		  try {
				String idx_str = base.substring(bas_idx + srch.length() + 1);
				String val_str = idx_str.substring(0, idx_str.indexOf('"'));
				rtrn = Math.round(Float.parseFloat(val_str) * mul);
		  } catch(NumberFormatException nfe) {
				rtrn = dflt; 
		  }
		} 
		
		return(rtrn);	  
  }

	/** 
	 * Loads a path
	 **/
  public ArrayList<Integer[]> ld_pth(String pth) {

    ArrayList<String> pth_ary = new ArrayList<String>(Arrays.asList(pth.split("\\s*L\\s*")));
		ArrayList<Integer[]> mov_path = new ArrayList<Integer[]>();
    int pth_len = pth_ary.size();
    Integer[] dflt_ary = {0, 0};
    
    for (int mbr = 0; mbr < pth_len; mbr += 1) {
      
      Integer[] xy_ary = new Integer[2];
      String[] xy_str = pth_ary.get(mbr).split("\\s* \\s*");
      
      for (int axs = 0; axs < 2; axs += 1) {

				try {
					xy_ary[axs] = Integer.parseInt(xy_str[axs]);
				} catch(NumberFormatException nfe) {
					xy_ary[axs] = dflt_ary[axs]; 
				}
				
				dflt_ary[axs] = xy_ary[axs];
			}
				
			mov_path.add(xy_ary);
    }

		return(mov_path);
	}	
	
	/** 
	 * Loads a path faster
	 **/
  public ArrayList<Integer[]> ld_svg_pth(char[] svg_chr) {
		
		ArrayList<Integer[]> mov_path = new ArrayList<Integer[]>();
    Integer[] dflt_ary = {0, 0};

		while (svg_chr[chr_idx - 1] != 'z') {

      Integer[] xy_ary = new Integer[2];

			try {
				xy_ary[0] = Integer.parseInt(cpyToChr(' ', svg_chr));
			} catch(NumberFormatException nfe) {
				xy_ary[0] = dflt_ary[0]; 
			}
			
			dflt_ary[0] = xy_ary[0];

			try {
				xy_ary[1] = Integer.parseInt(cpyToChrOrChr('L', 'z', svg_chr));
			} catch(NumberFormatException nfe) {
				xy_ary[1] = dflt_ary[1]; 
			}
			
			dflt_ary[1] = xy_ary[1];
			mov_path.add(xy_ary);
			chr_idx += 1;
		}

		return(mov_path);
	}

	private String cpyToChrOrChr(char tgt_1, char tgt_2, char[] svg_chr)
	{
		int init_idx = chr_idx;

		while (svg_chr[chr_idx] != tgt_1 && svg_chr[chr_idx] != tgt_2) {

			chr_idx += 1;
			
			if (chr_idx >= srch_len) break;
		}

		return(new String(copyOfRange(svg_chr, init_idx, chr_idx)));
	}
}

