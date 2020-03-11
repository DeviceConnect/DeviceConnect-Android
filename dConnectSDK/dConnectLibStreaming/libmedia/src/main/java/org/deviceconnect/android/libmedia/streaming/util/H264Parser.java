package org.deviceconnect.android.libmedia.streaming.util;

public final class H264Parser {

    private H264Parser() {
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
        int frame_crop_left_offset = 0;
        int frame_crop_right_offset = 0;
        int frame_crop_top_offset = 0;
        int frame_crop_bottom_offset = 0;

        int forbidden_zero_bit = scanner.readBits(1);
        int nal_ref_idc = scanner.readBits(2);
        int nal_unit_type = scanner.readBits(5);

        int profile_idc = scanner.readBits(8);
        int constraint_set0_flag = scanner.readBit();
        int constraint_set1_flag = scanner.readBit();
        int constraint_set2_flag = scanner.readBit();
        int constraint_set3_flag = scanner.readBit();
        int constraint_set4_flag = scanner.readBit();
        int constraint_set5_flag = scanner.readBit();
        int reserved_zero_2bits = scanner.readBits(2);
        int level_idc = scanner.readBits(8);
        int seq_parameter_set_id = scanner.readExponentialGolombCode();

        if (profile_idc == 100 || profile_idc == 110 ||
                profile_idc == 122 || profile_idc == 244 ||
                profile_idc == 44 || profile_idc == 83 ||
                profile_idc == 86 || profile_idc == 118) {
            int chroma_format_idc = scanner.readExponentialGolombCode();

            if (chroma_format_idc == 3) {
                int residual_colour_transform_flag = scanner.readBit();
            }
            int bit_depth_luma_minus8 = scanner.readExponentialGolombCode();
            int bit_depth_chroma_minus8 = scanner.readExponentialGolombCode();
            int qpprime_y_zero_transform_bypass_flag = scanner.readBit();
            int seq_scaling_matrix_present_flag = scanner.readBit();
            if (seq_scaling_matrix_present_flag != 0) {
                int i = 0;
                for (i = 0; i < 8; i++) {
                    int seq_scaling_list_present_flag = scanner.readBit();
                    if (seq_scaling_list_present_flag != 0) {
                        int sizeOfScalingList = (i < 6) ? 16 : 64;
                        int lastScale = 8;
                        int nextScale = 8;
                        int j = 0;
                        for (j = 0; j < sizeOfScalingList; j++) {
                            if (nextScale != 0) {
                                int delta_scale = scanner.readSE();
                                nextScale = (lastScale + delta_scale + 256) % 256;
                            }
                            lastScale = (nextScale == 0) ? lastScale : nextScale;
                        }
                    }
                }
            }
        }

        int log2_max_frame_num_minus4 = scanner.readExponentialGolombCode();
        int pic_order_cnt_type = scanner.readExponentialGolombCode();
        if (pic_order_cnt_type == 0) {
            int log2_max_pic_order_cnt_lsb_minus4 = scanner.readExponentialGolombCode();
        } else if (pic_order_cnt_type == 1) {
            int delta_pic_order_always_zero_flag = scanner.readBit();
            int offset_for_non_ref_pic = scanner.readSE();
            int offset_for_top_to_bottom_field = scanner.readSE();
            int num_ref_frames_in_pic_order_cnt_cycle = scanner.readExponentialGolombCode();
            int i;
            for (i = 0; i < num_ref_frames_in_pic_order_cnt_cycle; i++) {
                scanner.readSE();
            }
        }
        int max_num_ref_frames = scanner.readExponentialGolombCode();
        int gaps_in_frame_num_value_allowed_flag = scanner.readBit();
        int pic_width_in_mbs_minus1 = scanner.readExponentialGolombCode();
        int pic_height_in_map_units_minus1 = scanner.readExponentialGolombCode();
        int frame_mbs_only_flag = scanner.readBit();
        if (frame_mbs_only_flag == 0) {
            int mb_adaptive_frame_field_flag = scanner.readBit();
        }
        int direct_8x8_inference_flag = scanner.readBit();
        int frame_cropping_flag = scanner.readBit();
        if (frame_cropping_flag != 0) {
            frame_crop_left_offset = scanner.readExponentialGolombCode();
            frame_crop_right_offset = scanner.readExponentialGolombCode();
            frame_crop_top_offset = scanner.readExponentialGolombCode();
            frame_crop_bottom_offset = scanner.readExponentialGolombCode();
        }
        int vui_parameters_present_flag = scanner.readBit();

        int width = ((pic_width_in_mbs_minus1 + 1) * 16) - frame_crop_right_offset * 2 - frame_crop_left_offset * 2;
        int height = ((2 - frame_mbs_only_flag) * (pic_height_in_map_units_minus1 + 1) * 16) - (frame_crop_bottom_offset * 2) - (frame_crop_top_offset * 2);

        return new Sps(width, height);
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
