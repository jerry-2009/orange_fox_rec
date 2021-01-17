package com.fordownloads.orangefox.utils;

import android.app.Activity;
import android.content.Context;

import com.fordownloads.orangefox.R;
import com.fordownloads.orangefox.recycler.RecyclerItems;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ParseORS {
    ArrayList<RecyclerItems> items = new ArrayList<>();
    Activity a;
    boolean hasErrors;
    String noArgs;
    String[] knownParts, knownRebs;

    public ParseORS(Activity context) {
        this.a = context;
        this.noArgs = getString(R.string.script_no_params);
        this.knownParts = a.getResources().getStringArray(R.array.backup_list);
        this.knownRebs = a.getResources().getStringArray(R.array.reboot_list);
    }

    public ArrayList<RecyclerItems> parse(File file) throws IOException {
        items.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new SuFileInputStream(file)))) {
            String line = reader.readLine();
            while (line != null) {
                RecyclerItems i = processLine(line);
                if (i != null) items.add(i);
                line = reader.readLine();
            }
        }
        return items;
    }

    private String getString(int res) {
        return a.getString(res);
    }

    public boolean hasErrors() {
        return this.hasErrors;
    }

    private RecyclerItems processLine(String fullCmd) {
        String[] split = fullCmd.split(" ");
        String cmd = split[0].trim();
        String val = split.length == 1 ? "" : split[1];
        String val2 = split.length == 3 ? split[2] : "";
        switch (cmd) {
            case "": return null;
            case "install":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_zip), val,
                        R.drawable.ic_outline_archive_24, fullCmd);
            case "wipe":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_wipe),
                        Tools.cap(val.replace("/", "") + ";"),
                        R.drawable.ic_delete, fullCmd);
            case "backup":
                hasErrors = val.equals("");
                StringBuilder valBuilder = new StringBuilder();
                if (!val2.equals("")) valBuilder.append(val2).append("\n");
                valBuilder.append(constructParts(val));
                return new RecyclerItems(getString(R.string.script_backup), valBuilder.toString(),
                        R.drawable.ic_outline_cloud_download_24, fullCmd);
            case "restore":
                hasErrors = val.equals("");
                StringBuilder valBuilder2 = new StringBuilder();
                valBuilder2.append(val);
                if (!val2.equals("")) valBuilder2.append("\n").append(constructParts(val2));
                return new RecyclerItems(getString(R.string.script_restore), valBuilder2.toString(),
                        R.drawable.ic_outline_backup_24, fullCmd);
            case "remountrw":
                return new RecyclerItems(getString(R.string.script_rw),
                        "System", R.drawable.ic_round_sync_24, fullCmd);
            case "mount":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_mount),
                        Tools.cap(val), R.drawable.ic_outline_dns_24, fullCmd);
            case "unmount":
            case "umount":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_umount),
                        Tools.cap(val), R.drawable.ic_outline_umount_24, fullCmd);
            case "set":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_set),
                        val + " = " + val2, R.drawable.ic_equals, fullCmd);
            case "mkdir":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_mkdir),
                        val, R.drawable.ic_baseline_folder_open_24, fullCmd);
            case "reboot":
                switch (val) {
                    case "": val = knownRebs[0]; break;
                    case "recovery": val = knownRebs[1]; break;
                    case "poweroff": val = knownRebs[2]; break;
                    case "bootloader": val = knownRebs[3]; break;
                    case "download": val = knownRebs[4]; break;
                    case "edl": val = knownRebs[5]; break;
                    default: val = Tools.cap(val);
                }
                return new RecyclerItems(getString(R.string.script_reboot),
                        val, R.drawable.ic_round_refresh_24, fullCmd);
            case "cmd":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_cmd), val,
                        R.drawable.ic_terminal, fullCmd);
            case "print":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_print),
                        val, R.drawable.ic_outline_message_24, fullCmd);
            case "sideload":
                return new RecyclerItems(getString(R.string.script_sideload),
                        noArgs, R.drawable.ic_baseline_devices_24, fullCmd);
            case "fixperms":
            case "fixpermissions":
            case "fixcontexts":
                return new RecyclerItems(getString(R.string.script_fixperms),
                        noArgs, R.drawable.ic_outline_build_24, fullCmd);
            case "decrypt":
                hasErrors = val.equals("");
                return new RecyclerItems(getString(R.string.script_decrypt),
                        val, R.drawable.ic_baseline_lock_open_24, fullCmd);
            case "listmounts":
                return new RecyclerItems(getString(R.string.script_print_mounts),
                        noArgs, R.drawable.ic_outline_message_24, fullCmd);
            default:
                return new RecyclerItems(getString(R.string.unknown),
                        fullCmd, R.drawable.ic_round_close_24, fullCmd);
        }
    }

    private String constructParts(String val) {
        if (val.equals("")) return noArgs;

        StringBuilder parts = new StringBuilder();
        String[] split = val.toLowerCase().split("");
        boolean optimize = false;
        boolean digest = false;

        for (String part : split) {
            switch (part) {
                case "s": parts.append(knownParts[3]); break;
                case "d": parts.append(knownParts[2]); break;
                case "c": parts.append(knownParts[4]); break;
                case "r": parts.append(knownParts[1]); break;
                case "b": parts.append(knownParts[0]); break;
                case "a": parts.append("Android Secure"); break;
                case "e": parts.append("SD-EXT"); break;
                case "o": optimize = true; continue;
                case "m": digest = true; continue;
            }
            parts.append("; ");
        }
        parts.deleteCharAt(parts.length() - 2);

        if (optimize) parts.append("\n").append(getString(R.string.backup_optimize));
        if (digest) parts.append("\n").append(getString(R.string.backup_digest));

        return parts.toString();
    }
}