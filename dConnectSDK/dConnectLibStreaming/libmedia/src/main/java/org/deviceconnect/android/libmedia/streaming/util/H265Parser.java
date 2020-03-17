package org.deviceconnect.android.libmedia.streaming.util;

public final class H265Parser {

    private H265Parser() {
    }

    public static Sps parseSps(byte[] data) {
        return parseSps(data, 0);
    }

    public static Sps parseSps(byte[] data, int offset) {
        BitScanner scanner = new BitScanner(data);
        scanner.setOffset(offset);
        return parseSps(scanner);
    }

    private static Sps parseSps(BitScanner scanner) {
        int sps_video_parameter_set_id = scanner.readBits(4);
        int sps_max_sub_layers_minus1 = scanner.readBits(3);
        int sps_temporal_id_nesting_flag = scanner.readBits(1);
        profile_tier_level(scanner, true, sps_max_sub_layers_minus1);
        int sps_seq_parameter_set_id = scanner.readExponentialGolombCode();
        int chroma_format_idc = scanner.readExponentialGolombCode();
        int separate_colour_plane_flag;
        if (chroma_format_idc == 3) {
            separate_colour_plane_flag = scanner.readBits(1);
        }
        int pic_width_in_luma_samples = scanner.readExponentialGolombCode();
        int pic_height_in_luma_samples = scanner.readExponentialGolombCode();

        int conformance_window_flag = scanner.readBits(1);
        if (conformance_window_flag == 1) {
            int conf_win_left_offset = scanner.readExponentialGolombCode();
            int conf_win_right_offset = scanner.readExponentialGolombCode();
            int conf_win_top_offset = scanner.readExponentialGolombCode();
            int conf_win_bottom_offset = scanner.readExponentialGolombCode();
        }

        int bit_depth_luma_minus8 = scanner.readExponentialGolombCode();
        int bit_depth_chroma_minus8 = scanner.readExponentialGolombCode();
        int log2_max_pic_order_cnt_lsb_minus4 = scanner.readExponentialGolombCode();
        int sps_sub_layer_ordering_info_present_flag = scanner.readBits(1);
        int[] sps_max_dec_pic_buffering_minus1 = new int[sps_max_sub_layers_minus1];
        int[] sps_max_num_reorder_pics = new int[sps_max_sub_layers_minus1];
        int[] sps_max_latency_increase_plus1 = new int[sps_max_sub_layers_minus1];
        for (int i = (sps_sub_layer_ordering_info_present_flag == 1 ? 0 : sps_max_sub_layers_minus1); i <= sps_max_sub_layers_minus1; i++) {
            sps_max_dec_pic_buffering_minus1[i] = scanner.readExponentialGolombCode();
            sps_max_num_reorder_pics[i] = scanner.readExponentialGolombCode();
            sps_max_latency_increase_plus1[i] = scanner.readExponentialGolombCode();
        }

        int log2_min_luma_coding_block_size_minus3 = scanner.readExponentialGolombCode();
        int log2_diff_max_min_luma_coding_block_size = scanner.readExponentialGolombCode();
        int log2_min_luma_transform_block_size_minus2 = scanner.readExponentialGolombCode();
        int log2_diff_max_min_luma_transform_block_size = scanner.readExponentialGolombCode();
        int max_transform_hierarchy_depth_inter = scanner.readExponentialGolombCode();
        int max_transform_hierarchy_depth_intra = scanner.readExponentialGolombCode();
        int scaling_list_enabled_flag = scanner.readBits(1);
        if (scaling_list_enabled_flag == 1) {
            int sps_scaling_list_data_present_flag = scanner.readBits(1);
            if (sps_scaling_list_data_present_flag == 1) {
                scaling_list_data(scanner);
            }
        }

        int amp_enabled_flag = scanner.readBits(1);
        int sample_adaptive_offset_enabled_flag = scanner.readBits(1);
        int pcm_enabled_flag = scanner.readBits(1);
        if (pcm_enabled_flag == 1) {
            int pcm_sample_bit_depth_luma_minus1 = scanner.readBits(4);
            int pcm_sample_bit_depth_chroma_minus1 = scanner.readBits(4);
            int log2_min_pcm_luma_coding_block_size_minus3 = scanner.readExponentialGolombCode();
            int log2_diff_max_min_pcm_luma_coding_block_size = scanner.readExponentialGolombCode();
            int pcm_loop_filter_disabled_flag = scanner.readBits(1);
        }
        int num_short_term_ref_pic_sets = scanner.readExponentialGolombCode();
        for (int i = 0; i < num_short_term_ref_pic_sets; i++) {
            st_ref_pic_set(scanner, i);
        }

        int long_term_ref_pics_present_flag = scanner.readBits(1);
        if (long_term_ref_pics_present_flag == 1) {
            int num_long_term_ref_pics_sps = scanner.readExponentialGolombCode();
            int[] lt_ref_pic_poc_lsb_sps = new int[num_long_term_ref_pics_sps];
            int[] used_by_curr_pic_lt_sps_flag = new int[num_long_term_ref_pics_sps];
            for (int i = 0; i < num_long_term_ref_pics_sps; i++) {
                lt_ref_pic_poc_lsb_sps[i] = scanner.readExponentialGolombCode();
                used_by_curr_pic_lt_sps_flag[i] = scanner.readBits(1);
            }
        }

        int sps_temporal_mvp_enabled_flag = scanner.readBits(1);
        int strong_intra_smoothing_enabled_flag = scanner.readBits(1);
        int vui_parameters_present_flag = scanner.readBits(1);
        if (vui_parameters_present_flag == 1) {
            vui_parameters(scanner);
        }

        int sps_extension_present_flag = scanner.readBits(1);

        if (sps_extension_present_flag == 1) {
            int sps_range_extension_flag = scanner.readBits(1);
            int sps_multilayer_extension_flag = scanner.readBits(1);
            int sps_3d_extension_flag = scanner.readBits(1);
            int sps_extension_5bits = scanner.readBits(5);
        }

        return null;
    }

    private static void scaling_list_data(BitScanner scanner) {

    }

    private static void st_ref_pic_set(BitScanner scanner, int i) {

    }

    private static void vui_parameters(BitScanner scanner) {

    }

    private static void profile_tier_level(BitScanner scanner, boolean profilePresentFlag, int maxNumSubLayersMinus1) {
        if (profilePresentFlag) {
            int general_profile_space = scanner.readBits(2);
            int general_tier_flag = scanner.readBits(1);
            int general_profile_idc = scanner.readBits(5);
            int[] general_profile_compatibility_flag = new int[32];
            for (int j = 0; j < 32; j++) {
                general_profile_compatibility_flag[j] = scanner.readBits(1);
            }
            int general_progressive_source_flag = scanner.readBits(1);
            int general_interlaced_source_flag = scanner.readBits(1);
            int general_non_packed_constraint_flag = scanner.readBits(1);
            int general_frame_only_constraint_flag = scanner.readBits(1);
            if (general_profile_idc == 4 || general_profile_compatibility_flag[4] == 1 ||
                    general_profile_idc == 5 || general_profile_compatibility_flag[5] == 1 ||
                    general_profile_idc == 6 || general_profile_compatibility_flag[6] == 1 ||
                    general_profile_idc == 7 || general_profile_compatibility_flag[7] == 1) {
                int general_max_12bit_constraint_flag = scanner.readBits(1);
                int general_max_10bit_constraint_flag = scanner.readBits(1);
                int general_max_8bit_constraint_flag = scanner.readBits(1);
                int general_max_422chroma_constraint_flag = scanner.readBits(1);
                int general_max_420chroma_constraint_flag = scanner.readBits(1);
                int general_max_monochrome_constraint_flag = scanner.readBits(1);
                int general_intra_constraint_flag = scanner.readBits(1);
                int general_one_picture_only_constraint_flag = scanner.readBits(1);
                int general_lower_bit_rate_constraint_flag = scanner.readBits(1);
                long general_reserved_zero_34bits = scanner.readBits(34);
            } else {
                long general_reserved_zero_43bits = scanner.readBits(43);
            }

            if ((general_profile_idc >= 1 && general_profile_idc <= 5) ||
                    general_profile_compatibility_flag[1] == 1 ||
                    general_profile_compatibility_flag[2] == 1 ||
                    general_profile_compatibility_flag[3] == 1 ||
                    general_profile_compatibility_flag[4] == 1 ||
                    general_profile_compatibility_flag[5] == 1) {
                int general_inbld_flag = scanner.readBits(1);
            } else {
                int general_reserved_zero_bit = scanner.readBits(1);
            }
        }

        int general_level_idc = scanner.readBits(8);

        int[] sub_layer_profile_present_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_level_present_flag = new int[maxNumSubLayersMinus1];
        for (int i = 0; i < maxNumSubLayersMinus1; i++) {
            sub_layer_profile_present_flag[i] = scanner.readBits(1);
            sub_layer_level_present_flag[i] = scanner.readBits(1);
        }

        if (maxNumSubLayersMinus1 > 0) {
            int[] reserved_zero_2bits = new int[8];
            for (int i = maxNumSubLayersMinus1; i < 8; i++) {
                reserved_zero_2bits[i] = scanner.readBits(2);
            }
        }

        int[] sub_layer_profile_space = new int[maxNumSubLayersMinus1];
        int[] sub_layer_tier_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_profile_idc = new int[maxNumSubLayersMinus1];
        int[][] sub_layer_profile_compatibility_flag = new int[maxNumSubLayersMinus1][32];
        int[] sub_layer_progressive_source_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_interlaced_source_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_non_packed_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_frame_only_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_max_12bit_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_max_10bit_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_max_8bit_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_max_422chroma_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_max_420chroma_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_max_monochrome_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_intra_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_one_picture_only_constraint_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_lower_bit_rate_constraint_flag = new int[maxNumSubLayersMinus1];
        long[] sub_layer_reserved_zero_34bits = new long[maxNumSubLayersMinus1];
        long[] sub_layer_reserved_zero_43bits = new long[maxNumSubLayersMinus1];
        int[] sub_layer_inbld_flag = new int[maxNumSubLayersMinus1];
        int[] sub_layer_reserved_zero_bit = new int[maxNumSubLayersMinus1];
        int[] sub_layer_level_idc = new int[maxNumSubLayersMinus1];
        for (int i = 0; i < maxNumSubLayersMinus1; i++) {
            if (sub_layer_profile_present_flag[i] == 1) {
                sub_layer_profile_space[i] = scanner.readBits(2);
                sub_layer_tier_flag[i] = scanner.readBits(1);
                sub_layer_profile_idc[i] = scanner.readBits(5);
                for (int j = 0; j < 32; j++) {
                    sub_layer_profile_compatibility_flag[i][j] = scanner.readBits(1);
                }
                sub_layer_progressive_source_flag[i] = scanner.readBits(1);
                sub_layer_interlaced_source_flag[i] = scanner.readBits(1);
                sub_layer_non_packed_constraint_flag[i] = scanner.readBits(1);
                sub_layer_frame_only_constraint_flag[i] = scanner.readBits(1);
                if (sub_layer_profile_idc[i] == 4 || sub_layer_profile_compatibility_flag[i][4] == 1 ||
                        sub_layer_profile_idc[i] == 5 || sub_layer_profile_compatibility_flag[i][5] == 1 ||
                        sub_layer_profile_idc[i] == 6 || sub_layer_profile_compatibility_flag[i][6] == 1 ||
                        sub_layer_profile_idc[i] == 7 || sub_layer_profile_compatibility_flag[i][7] == 1) {
                    sub_layer_max_12bit_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_max_10bit_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_max_8bit_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_max_422chroma_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_max_420chroma_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_max_monochrome_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_intra_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_one_picture_only_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_lower_bit_rate_constraint_flag[i] = scanner.readBits(1);
                    sub_layer_reserved_zero_34bits[i] = scanner.readBits(34);
                } else {
                    sub_layer_reserved_zero_43bits[i] = scanner.readBits(43);
                }

                if ((sub_layer_profile_idc[i] >= 1 && sub_layer_profile_idc[i] <= 5) ||
                        sub_layer_profile_compatibility_flag[i][1] == 1 ||
                        sub_layer_profile_compatibility_flag[i][2] == 1 ||
                        sub_layer_profile_compatibility_flag[i][3] == 1 ||
                        sub_layer_profile_compatibility_flag[i][4] == 1 ||
                        sub_layer_profile_compatibility_flag[i][5] == 1) {
                    sub_layer_inbld_flag[i] = scanner.readBits(1);
                } else {
                    sub_layer_reserved_zero_bit[i] = scanner.readBits(1);
                }
            }

            if (sub_layer_level_present_flag[i] == 1) {
                sub_layer_level_idc[i] = scanner.readBits(8);
            }
        }
    }

    public static class Sps {
        private int mWidth;
        private int mHeight;

        Sps(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        @Override
        public String toString() {
            return "Sps{" +
                    "mWidth=" + mWidth +
                    ", mHeight=" + mHeight +
                    '}';
        }
    }
}
