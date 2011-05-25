function proc_spikes (path,filename,bin_size,a,b)

cmd = sprintf ('load %s/%s;',path,filename);
eval(cmd);
cmd = sprintf ('spikes = %s;',filename);
cmd = strrep (cmd,'.spk','');
eval (cmd);
spikes_name = strrep(filename,'.spk','');
disp (build_java_code(a,b,bin_size,spikes,spikes_name));
