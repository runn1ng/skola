#system("mv ga.properties ga.orig");
for my $i (1..15) {
#ruleta turnament chromosome standard
for my $fst (1,2,3,4 ){
my @first_option;
if ($fst==1) {@first_option=(1)}
if ($fst==2) {for my $f (10..1) {for my $s (1..20) {my $ff=int($i*10/$f); my $ss=1/$s; push @first_option, $ff."-".$ss}}}
if ($fst==3) {@first_option=(1..20);}
if ($fst==4) {@first_option=(1);}
for my $fst_opt (@first_option){
for my $snd (1,2,3,4 ){
my @second_option;
if ($snd=1) {@second_option=(1)}
if ($snd==2) {for my $f (10..1) {for my $s (1..20) {my $ff=int($i*10/$f); my $ss=1/$s; push @second_option, $ff."-".$ss}}}
if ($snd==3) {@second_option=(1..20);}
if ($snd==4) {@second_option=(1);}
for my $snd_opt(@second_option){
for my $max_gen(1..15){
for my $sort(0,1){
for my $preserve(0,1){

	my $d=<<EOF;
	#pocet bitu v jedinci
	bins = 10	

	#velikost populace
	population_size = POP
	#maximalni pocet generaci
	max_generations = 50

	#kolikrat se ma experiment opakovat
	repeats = 10
	#prefix jmena souboru jednotlivych experimentu
	log_filename_prefix = stat.log
	#jmeno souboru s konecnymi vysledky (minima, maxima a prumery jednotlivych experimentu)
	results_filename = results.log
	first_selector_type = FST	
	first_more = FMR
	second_selector_type = SND
	second_selector_type_more = SMR
	sort = SRT
	preserve = PRS

	#jmeno souboru s vyvojem rozdilu mezi nejtezsi a nejlehci hromadkou
	prog_file_prefix = prog.log
	#jmeno souboru se statistikami vyvoje rozdilu
	prog_file_results = progress_POP_FST_FMR_SND_SMR_SRT_PRS.log
	#prefix jmena souboru s nejlepsim jedincem
	best_ind_prefix = best
EOF

	my $h = $i*10;
	$d=~s/POP/$h/g;
	$d=~s/FST/$fst/g;
	$d=~s/FMR/$fst_opt/g;
	$d=~s/SND/$snd/g;
	$d=~s/SMR/$snd_opt/g;
	$d=~s/SRT/$sort/g;
	$d=~s/PRS/$preserve/g;

	$d=~/(progress_.*\.log)/;
	my $name=$1;
	system("touch $name");

	open $o, ">ga.properties";
	print $o $d;

	#system("java -cp './knihovna/jgap.jar:./knihovna/lib/*:./build/' eva2010.cv1.BitGA");

	system("rm stat.log.*");
	system("rm prog.log.*");
	system("rm results.log");
	system("rm best");

	if (defined $s) {$s.=","}
	$s .= $name.' using 2 w l';

}}}}}}}}

system("csdgnuplot -e '
set terminal png;
set output \"graf.png\";
plot $s'");

system("mkdir vysledky/cv2; mv graf.png vysledky/cv2; mv progress_* vysledky/cv2");
#system("mv ga.orig ga.properties");
